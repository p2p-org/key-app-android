package org.p2p.wallet.root

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import timber.log.Timber
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class RootPresenter(
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val sellInteractor: SellInteractor
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
                awaitAll(
                    async { sellInteractor.loadSellAvailability() },
                    async { orcaInfoInteractor.load() },
                    async { feeRelayerAccountInteractor.getRelayInfo() }
                )
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial data")
            }
        }
    }
}
