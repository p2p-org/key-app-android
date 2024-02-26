package org.p2p.wallet.send.ui.details

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.send.model.SendSolanaFee

interface NewSendDetailsContract {

    interface View : MvpView {
        fun showAccountCreationFeeLoading(isLoading: Boolean)
        fun showNoTokensScreen(tokens: List<Token.Active>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadFeePayerTokens(
            fee: SendSolanaFee,
            inputAmount: BigInteger,
            useMax: Boolean
        )
    }
}
