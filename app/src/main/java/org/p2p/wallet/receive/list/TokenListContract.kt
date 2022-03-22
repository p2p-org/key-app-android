package org.p2p.wallet.receive.list

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.user.model.TokenData

interface TokenListContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showItems(items: List<TokenData>, scrollToUp: Boolean)
        fun showEmpty(searchText: String)
        fun reset()
    }

    interface Presenter : MvpPresenter<View> {
        fun load(isRefresh: Boolean, scrollToUp: Boolean = false)
        fun search(text: CharSequence?)
    }
}
