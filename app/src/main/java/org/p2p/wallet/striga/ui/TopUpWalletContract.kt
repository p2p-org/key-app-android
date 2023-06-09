package org.p2p.wallet.striga.ui

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface TopUpWalletContract {
    interface View : MvpView {

        fun hideStrigaBankTransferView()
        fun showStrigaBankTransferView()
        fun showBankCardView(tokenToBuy: Token)
        fun hideBankCardView()
        fun showCryptoReceiveView()
    }
    interface Presenter : MvpPresenter<View>
}
