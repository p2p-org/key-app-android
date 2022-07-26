package org.p2p.wallet.root

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class RootPresenter(
    private val userInteractor: UserInteractor,
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    override fun loadPricesAndBids() {
        launch {
            try {
                userInteractor.loadAllTokensData()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            }
        }
    }
}
