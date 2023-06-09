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

        fun hideStrigaBankTransferView()
        fun showStrigaBankTransferView(
            navigationTarget: BankTransferNavigationTarget = BankTransferNavigationTarget.StrigaOnboarding,
            showProgress: Boolean = false
        )
        fun showBankCardView(tokenToBuy: Token)
        fun hideBankCardView()
        fun showCryptoReceiveView()
    }
    interface Presenter : MvpPresenter<View>
}
