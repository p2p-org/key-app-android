package org.p2p.wallet.svl.ui.receive

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.svl.interactor.SendViaLinkReceiveFundsInteractor

class ReceiveViaLinkPresenter(
    private val interactor: SendViaLinkReceiveFundsInteractor
) : BasePresenter<ReceiveViaLinkContract.View>(),
    ReceiveViaLinkContract.Presenter {

    override fun attach(view: ReceiveViaLinkContract.View) {
        super.attach(view)
        // todo: PWN-7422 - add request for balance and tokens info
    }

    override fun claimToken() {
    }
}
