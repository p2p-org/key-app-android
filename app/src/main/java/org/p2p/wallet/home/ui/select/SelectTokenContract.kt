package org.p2p.wallet.home.ui.select

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token

interface SelectTokenContract {
    interface View : MvpView {
        fun showTokens(items: List<Token>)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun search(searchText: String)
    }
}
