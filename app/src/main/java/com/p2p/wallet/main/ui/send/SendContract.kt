package com.p2p.wallet.main.ui.send

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.main.ui.info.TransactionInfo
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showSuccess(info: TransactionInfo)
        fun showWrongWalletError()
        fun navigateToTokenSelection(tokens: List<Token>)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun sendToken(targetAddress: String, amount: BigDecimal)
        fun loadInitialData()
        fun loadTokensForSelection()
        fun setSourceToken(newToken: Token)
    }
}