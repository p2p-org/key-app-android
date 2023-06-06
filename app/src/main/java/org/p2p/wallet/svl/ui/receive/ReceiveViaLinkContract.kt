package org.p2p.wallet.svl.ui.receive

import org.p2p.core.token.Token
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.ui.error.SendViaLinkError

interface ReceiveViaLinkContract {
    interface View : MvpView {
        fun renderClaimTokenDetails(
            tokenAmount: TextViewCellModel,
            sentFromAddress: TextViewCellModel,
            tokenIcon: IconWrapperCellModel
        )

        fun renderState(state: SendViaLinkClaimingState)
        fun showButtonLoading(isLoading: Boolean)
        fun showLinkError(error: SendViaLinkError)
    }

    interface Presenter : MvpPresenter<View> {
        fun claimToken(temporaryAccount: TemporaryAccount, token: Token.Active)
        fun parseAccountFromLink(link: SendViaLinkWrapper, isRetry: Boolean = false)
    }
}
