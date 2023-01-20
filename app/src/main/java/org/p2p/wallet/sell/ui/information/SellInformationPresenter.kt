package org.p2p.wallet.sell.ui.information

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.sell.interactor.SellInteractor

class SellInformationPresenter(
    val interactor: SellInteractor,
) : BasePresenter<SellInformationContract.View>(), SellInformationContract.Presenter {

    override fun onOkClick(notShowAgain: Boolean) {
        if (notShowAgain) interactor.doNotShowInformDialogAgain()
        view?.dismissWithOkResult()
    }
}
