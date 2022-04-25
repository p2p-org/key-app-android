package org.p2p.wallet.renbtc.ui.status

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor

class RenStatusesPresenter(
    private val renBtcInteractor: RenBtcInteractor
) : BasePresenter<RenStatusesContract.View>(),
    RenStatusesContract.Presenter {

    override fun subscribe(transactionId: String) {
        launch {
            val statuses = renBtcInteractor.getTransactionStatuses(transactionId)
            if (statuses != null) {
                view?.showStatuses(statuses)
            } else {
                view?.showStatusesNotFound()
            }
        }
    }
}
