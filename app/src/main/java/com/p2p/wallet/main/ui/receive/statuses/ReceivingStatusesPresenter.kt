package com.p2p.wallet.main.ui.receive.statuses

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.RenBTCInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReceivingStatusesPresenter(
    private val interactor: RenBTCInteractor
) : BasePresenter<ReceivingStatusesContract.View>(),
    ReceivingStatusesContract.Presenter {

    override fun subscribe() {
        launch {
            interactor.getPaymentDataFlow().collect {
//                view?.showData(it)
            }
        }
    }
}