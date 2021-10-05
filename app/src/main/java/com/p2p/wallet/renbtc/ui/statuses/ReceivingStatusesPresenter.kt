package com.p2p.wallet.renbtc.ui.statuses

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.renbtc.interactor.RenBtcInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReceivingStatusesPresenter(
    private val interactor: RenBtcInteractor
) : BasePresenter<ReceivingStatusesContract.View>(),
    ReceivingStatusesContract.Presenter {

    override fun subscribe() {
        launch {
            interactor.getRenVMStatusFlow().collect { view?.showData(it) }
        }
    }
}