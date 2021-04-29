package com.p2p.wallet.main.ui.send

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.dashboard.model.local.Token

interface SendContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showSuccess()
        fun navigateToTokenSelection(tokens: List<Token>)
        fun showFullScreenLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun sendToken(targetAddress: String, amount: Double)
        fun loadInitialData()
        fun loadTokensForSelection()
        fun setSourceToken(newToken: Token)
    }
}