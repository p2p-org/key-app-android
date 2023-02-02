package org.p2p.wallet.moonpay.ui.transaction

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface SellTransactionDetailsContract {
    interface View : MvpView {
        fun close()
        fun showLoading(isLoading: Boolean)
        fun navigateToSendScreen(
            tokenToSend: Token.Active,
            sendAmount: BigDecimal,
            receiverAddress: String
        )

        fun renderViewState(viewState: SellTransactionDetailsViewState)
    }

    interface Presenter : MvpPresenter<View> {
        fun onRemoveFromHistoryClicked()
        fun onCancelTransactionClicked()
        fun onSendClicked()
    }
}
