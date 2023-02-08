package org.p2p.wallet.sell.ui.information

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.sell.interactor.SellInteractor

class SellInformationPresenter(
    private val interactor: SellInteractor,
) : BasePresenter<SellInformationContract.View>(), SellInformationContract.Presenter {

    override fun closeDialog(shouldShowAgain: Boolean) {
        launch {
            interactor.setShouldShowInformDialog(shouldShowAgain)
            view?.dismissWithOkResult()
        }
    }
}
