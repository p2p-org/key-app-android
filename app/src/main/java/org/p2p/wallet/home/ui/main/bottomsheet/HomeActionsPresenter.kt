package org.p2p.wallet.home.ui.main.bottomsheet

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.sell.interactor.SellInteractor
import kotlinx.coroutines.launch

class HomeActionsPresenter(
    private val sellInteractor: SellInteractor
) : BasePresenter<HomeActionsContract.View>(), HomeActionsContract.Presenter {
    override fun attach(view: HomeActionsContract.View) {
        super.attach(view)
        launch {
            view.setupHomeActions(isSellFeatureEnabled = sellInteractor.isSellAvailable())
        }
    }
}
