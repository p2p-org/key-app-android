package org.p2p.wallet.svl.ui.receive

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.utils.Base58String

interface ReceiveViaLinkContract {
    interface View : MvpView {
        fun renderClaimTokenDetails(
            amountInTokens: String,
            tokenSymbol: String,
            sentFromAddress: Base58String,
            tokenIconUrl: String,
            linkCreationDate: String
        )
        fun renderState(state: SendViaLinkClaimingState)
        fun navigateToErrorScreen(error: SendViaLinkError)
    }

    interface Presenter : MvpPresenter<View> {
        fun parseLink(link: SendViaLinkWrapper)
        fun claimToken(temporaryAccount: TemporaryAccount, amountInToken: String, tokenSymbol: String)
    }
}
