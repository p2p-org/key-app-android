package org.p2p.wallet.debug.uikit

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface DebugUiKitFragmentContract {
    interface View : MvpView {
        fun showViews(items: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun buildInformerViews()
    }
}
