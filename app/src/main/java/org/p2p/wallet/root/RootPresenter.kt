package org.p2p.wallet.root

import timber.log.Timber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.p2p.wallet.common.feature_toggles.toggles.remote.BooleanFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewSwapEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository

private const val TAG = "RootPresenter"

class RootPresenter(
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val sellInteractor: SellInteractor,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val newSwapEnabled: NewSwapEnabledFeatureToggle,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    override fun attach(view: RootContract.View) {
        super.attach(view)
        loadInitialData()
    }

    /**
     * In case if these requests are failed - it's not critical.
     * It will be loaded again when used
     * */
    private fun loadInitialData() {
        launch {
            try {
                getInitialDataRequests().awaitAll()
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Error loading initial data")
            }
        }
    }

    private fun getInitialDataRequests(): List<Deferred<Any>> = buildList {
        add(async { orcaInfoInteractor.load() })
        add(async { feeRelayerAccountInteractor.getRelayInfo() })

        addIfToggleEnabled(toggle = sellEnabledFeatureToggle, request = sellInteractor::loadSellAvailability)
        addIfToggleEnabled(toggle = newSwapEnabled, request = swapTokensRepository::getTokens)
    }

    private fun MutableList<Deferred<Any>>.addIfToggleEnabled(
        toggle: BooleanFeatureToggle,
        request: suspend () -> Any
    ) {
        if (toggle.isFeatureEnabled) {
            add(element = async { request.invoke() })
        } else {
            Timber.tag(TAG).i("Request for ${toggle.featureKey} won't add")
        }
    }
}
