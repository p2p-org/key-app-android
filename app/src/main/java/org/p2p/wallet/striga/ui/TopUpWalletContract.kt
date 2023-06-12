package org.p2p.wallet.striga.ui

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface TopUpWalletContract {

    enum class BankTransferNavigationTarget {
        Nowhere,
        StrigaOnboarding,
        StrigaSignupFirstStep,
        StrigaSignupSecondStep,
        StrigaSmsVerification,
        SumSubVerification,
    }

    interface View : MvpView {
        fun showStrigaBankTransferView(showProgress: Boolean = false)
        fun hideStrigaBankTransferView()
        fun navigateToBankTransferTarget(target: BankTransferNavigationTarget)
        fun showBankCardView(tokenToBuy: Token)
        fun hideBankCardView()
        fun showCryptoReceiveView()
    }
    interface Presenter : MvpPresenter<View> {
        fun onBankTransferClicked()
    }
}
