package org.p2p.wallet.home.ui.topup

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

interface TopUpWalletContract {

    interface View : MvpView {
        fun showStrigaBankTransferView(showProgress: Boolean = false)
        fun hideStrigaBankTransferView()
        fun navigateToBankTransferTarget(target: StrigaUserStatusDestination)
        fun navigateToKycPending()
        fun showBankCardView(tokenToBuy: Token)
        fun hideBankCardView()
        fun showCryptoReceiveView()
    }

    interface Presenter : MvpPresenter<View> {
        fun onBankTransferClicked()
    }
}
