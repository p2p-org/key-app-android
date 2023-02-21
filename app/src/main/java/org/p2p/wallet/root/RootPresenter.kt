package org.p2p.wallet.root

import timber.log.Timber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.p2p.core.utils.addIf
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewSwapEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository

private const val TAG = "RootPresenter"

class RootPresenter(
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val sellInteractor: SellInteractor,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val newSwapEnabledFeatureToggle: NewSwapEnabledFeatureToggle,
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

        addIf(
            predicate = sellEnabledFeatureToggle.isFeatureEnabled,
            value = async { sellInteractor.loadSellAvailability() }
        )
        addIf(
            predicate = newSwapEnabledFeatureToggle.isFeatureEnabled,
            async { swapRoutesRepository.loadAllSwapRoutes() },
            async { swapTokensRepository.getTokens() }
        )
    }
        .also {
            Timber.tag(TAG).i("Total requests added: ${it.size}")
        }
}
