package org.p2p.wallet.home.ui.main.bottomsheet

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository

class HomeActionsPresenter(
    private val moonpaySellRepository: MoonpaySellRepository
) : BasePresenter<HomeActionsContract.View>(), HomeActionsContract.Presenter {
    override fun attach(view: HomeActionsContract.View) {
        super.attach(view)
        view.setupHomeActions(moonpaySellRepository.isSellAllowedForUser())
    }
}
