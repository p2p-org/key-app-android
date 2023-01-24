package org.p2p.wallet.sell.ui.lock

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface SellLockedContract {

    interface View : MvpView {
        fun navigateBack()
        fun navigateBackToMain()
        fun navigateToSendScreen(tokenToSend: Token.Active, sendAmount: BigDecimal, receiverAddress: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun onCancelTransactionClicked()
        fun onSendClicked()
    }
}
