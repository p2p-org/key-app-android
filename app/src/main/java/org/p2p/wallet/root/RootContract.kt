package org.p2p.wallet.root

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError

interface RootContract {

    interface View : MvpView {
        fun showToast(@StringRes message: Int)
        fun showToast(message: String)
        fun showTransferLinkBottomSheet(state: TemporaryAccountState, deeplink: SendViaLinkWrapper)
        fun showLinkError(error: SendViaLinkError)
    }

    interface Presenter : MvpPresenter<View> {
        fun parseTransferAppLink(link: SendViaLinkWrapper)
    }
}
