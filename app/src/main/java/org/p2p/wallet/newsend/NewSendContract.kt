package org.p2p.wallet.newsend

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.token.Token

interface NewSendContract {
    interface View : MvpView {
        fun showTokenToSend(token: Token.Active)
        fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
    }

    interface Presenter : MvpPresenter<View> {
        fun setTokenToSend(newToken: Token.Active)
        fun onTokenClicked()
    }
}
