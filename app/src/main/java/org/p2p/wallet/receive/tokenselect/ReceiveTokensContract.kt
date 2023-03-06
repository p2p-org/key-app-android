package org.p2p.wallet.receive.tokenselect

import org.p2p.core.token.TokenData
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReceiveTokensContract {
    interface View : MvpView {
        fun setBannerTokens(firstTokenUrl: String, secondTokenUrl: String)
        fun showTokenItems(items: List<TokenData>, scrollToUp: Boolean)
        fun showEmptyState(isEmpty: Boolean)
        fun setBannerVisibility(isVisible: Boolean)
        fun reset()
    }

    interface Presenter : MvpPresenter<View> {
        fun load(isRefresh: Boolean, scrollToUp: Boolean = false)
        fun onSearchTokenQueryChanged(newQuery: String)
    }
}
