package org.p2p.wallet.home.ui.new

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.SelectTokenItem

interface NewSelectTokenContract {

    interface View : MvpView {
        fun showTokens(items: List<SelectTokenItem>)
        fun showEmptyState(isVisible: Boolean)
        fun scrollToTop()
        fun clearTokens()
        fun navigateBackWithToken(clickedToken: Token.Active)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(tokenNameQuery: String)
        fun onTokenClicked(clickedToken: Token.Active)
    }
}
