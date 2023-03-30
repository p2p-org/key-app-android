package org.p2p.wallet.svl.ui.receive

import org.p2p.core.token.Token
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError

interface ReceiveViaLinkContract {
    interface View : MvpView {
        fun renderClaimTokenDetails(
            tokenAmount: TextViewCellModel,
            sentFromAddress: TextViewCellModel,
            tokenIcon: ImageViewCellModel,
            currentDate: TextViewCellModel
        )

        fun renderState(state: SendViaLinkClaimingState)
        fun navigateToErrorScreen(error: SendViaLinkError)
    }

    interface Presenter : MvpPresenter<View> {
        fun claimToken(temporaryAccount: TemporaryAccount, token: Token.Active)
        fun handleState(state: TemporaryAccountState)
    }
}
