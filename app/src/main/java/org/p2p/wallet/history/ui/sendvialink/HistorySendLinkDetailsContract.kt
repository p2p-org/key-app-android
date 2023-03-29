package org.p2p.wallet.history.ui.sendvialink

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface HistorySendLinkDetailsContract {
    sealed interface ViewState {
        object Loading : ViewState
        data class Content(
            val link: String,
            val iconUrl: String?,
            val formattedAmountUsd: String,
            val formattedTokenAmount: String,
            val formattedDate: String
        ) : ViewState
    }

    interface View : MvpView {
        fun renderState(state: ViewState)
        fun close()
    }

    interface Presenter : MvpPresenter<View>
}
