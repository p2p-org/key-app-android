package org.p2p.wallet.svl.ui.linkgeneration

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.svl.model.TemporaryAccount

interface SendLinkGenerationContract {
    interface View : MvpView {
        fun showResult(state: LinkGenerationState)
    }

    interface Presenter : MvpPresenter<View> {
        fun generateLink(
            recipient: TemporaryAccount,
            token: Token.Active,
            lamports: BigInteger,
            isSimulation: Boolean,
            currencyModeSymbol: String
        )
    }
}
