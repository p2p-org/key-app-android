package org.p2p.wallet.root

import timber.log.Timber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.utils.addIf
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.home.ui.container.MainFragmentDeeplinkHandlerFactory
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.utils.unsafeLazy

private const val TAG = "RootPresenter"

class RootPresenter(
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val sellInteractor: SellInteractor,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val deeplinksManager: AppDeeplinksManager,
    private val deeplinkHandlerFactory: MainFragmentDeeplinkHandlerFactory,
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    private val deeplinkHandler by unsafeLazy {
        deeplinkHandlerFactory.create(
            navigator = view,
            scope = this,
        )
    }

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
        add(async { swapTokensRepository.getTokens() })
    }
        .also {
            Timber.tag(TAG).i("Total requests added: ${it.size}")
        }

    override fun observeDeeplinks() {
        launchSupervisor {
            val supportedTargets = setOf(
                DeeplinkTarget.BUY,
                DeeplinkTarget.SEND,
                DeeplinkTarget.SWAP,
                DeeplinkTarget.CASH_OUT,
                DeeplinkTarget.REFERRAL
            )
            deeplinksManager.subscribeOnDeeplinks(supportedTargets)
                .onEach(deeplinkHandler::handle)
                .launchIn(this)

            deeplinksManager.executeHomePendingDeeplink()
            deeplinksManager.executeTransferPendingAppLink()
        }
    }
}
