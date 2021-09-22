package com.p2p.wallet.renBTC.ui.statuses

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.renBTC.interactor.RenBTCInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReceivingStatusesPresenter(
    private val interactor: RenBTCInteractor
) : BasePresenter<ReceivingStatusesContract.View>(),
    ReceivingStatusesContract.Presenter {

    override fun subscribe() {
        launch {
            interactor.getRenVMStatusFlow().collect { view?.showData(it) }
        }
    }
}