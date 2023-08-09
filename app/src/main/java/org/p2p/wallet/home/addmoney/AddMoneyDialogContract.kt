package org.p2p.wallet.home.addmoney

import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.addmoney.model.AddMoneyItemType
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

interface AddMoneyDialogContract {

    interface View : MvpView {
        fun navigateToBankTransferTarget(target: StrigaUserStatusDestination)
        fun navigateToBankCard(tokenToBuy: Token, paymentMethod: PaymentMethod.MethodType)
        fun navigateToCrypto()
        fun navigateToKycPending()
        fun setCellItems(items: List<AnyCellItem>)
        fun showItemProgress(itemType: AddMoneyItemType, showProgress: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onItemClick(itemType: AddMoneyItemType)
    }
}
