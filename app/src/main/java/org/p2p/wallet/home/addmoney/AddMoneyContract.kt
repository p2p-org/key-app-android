package org.p2p.wallet.home.addmoney

import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

interface AddMoneyContract {

    interface View : MvpView {
        fun navigateToBankTransferTarget(target: StrigaUserStatusDestination)
        fun navigateToBankCard(tokenToBuy: Token, paymentMethod: PaymentMethod.MethodType)
        fun navigateToCrypto()
        fun navigateToKycPending()
        fun setCellItems(items: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onButtonClick(button: AddMoneyButton)
    }
}
