package org.p2p.wallet.main.ui.receive.list

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.user.model.TokenData

interface TokenListContract {

    interface View: MvpView {
        fun showLoading(isLoading: Boolean)
        fun showItems(items: List<TokenData>)
    }

    interface Presenter: MvpPresenter<View> {
        fun load()
        fun search(text: CharSequence?)
    }
}