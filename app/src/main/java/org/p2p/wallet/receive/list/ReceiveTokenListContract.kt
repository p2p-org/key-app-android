package org.p2p.wallet.receive.list

import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReceiveTokenListContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showItems(items: List<TokenMetadata>, scrollToUp: Boolean)
        fun showEmpty(searchText: String)
        fun reset()
    }

    interface Presenter : MvpPresenter<View> {
        fun load(isRefresh: Boolean, scrollToUp: Boolean = false)
        fun search(text: CharSequence?)
    }
}
