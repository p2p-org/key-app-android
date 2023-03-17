package org.p2p.wallet.home.ui.vialink

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.utils.Base58String

interface SendViaLinkReceiveFundsContract {
    interface View : MvpView {
        fun renderClaimTokenDetails(
            amountInTokens: String,
            tokenSymbol: String,
            sentFromAddress: Base58String,
            addressAsUsername: String?,
            tokenIconUrl: String,
            linkCreationDate: String
        )
        fun renderState(state: SendViaLinkReceiveFundsState)
    }

    interface Presenter : MvpPresenter<View> {
        fun claimToken()
    }
}
