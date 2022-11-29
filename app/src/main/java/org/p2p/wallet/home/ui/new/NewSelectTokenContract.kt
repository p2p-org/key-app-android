package org.p2p.wallet.home.ui.new

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.model.Token

interface NewSelectTokenContract {

    interface View : MvpView {
        fun clearTokens()
        fun showTokens(items: List<SelectTokenItem>)
        fun showEmptyState(isVisible: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(tokens: List<Token.Active>, selectedToken: Token.Active?)
        fun search(tokenNameQuery: String)
    }
}
