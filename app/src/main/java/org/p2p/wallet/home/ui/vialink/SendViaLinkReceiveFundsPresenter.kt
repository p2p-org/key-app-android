package org.p2p.wallet.home.ui.vialink

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.vialink.interactor.SendViaLinkReceiveFundsInteractor

class SendViaLinkReceiveFundsPresenter(
    private val interactor: SendViaLinkReceiveFundsInteractor
) : BasePresenter<SendViaLinkReceiveFundsContract.View>(),
    SendViaLinkReceiveFundsContract.Presenter {

    override fun attach(view: SendViaLinkReceiveFundsContract.View) {
        super.attach(view)
        // todo: PWN-7422 - add request for balance and tokens info
    }

    override fun claimToken() {
    }
}
