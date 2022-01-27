package org.p2p.wallet.main.ui.receive.network

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor

class ReceiveNetworkTypePresenter(
    private val renBtcInteractor: RenBtcInteractor
) : BasePresenter<ReceiveNetworkTypeContract.View>(),
    ReceiveNetworkTypeContract.Presenter {

    override fun onNetworkChanged(type: NetworkType) {
        launch {
            when (type) {
                NetworkType.SOLANA -> {
                    view?.navigateToReceive(type)
                }
                NetworkType.BITCOIN -> {
                    val session = renBtcInteractor.findActiveSession()
                    if (session != null && session.isValid) {
                        view?.navigateToReceive(type)
                    } else {
                        view?.showNetworkInfo(type)
                    }
                }
            }
        }
    }

    override fun confirm(type: NetworkType) {
        view?.navigateToReceive(type)
    }
}