package org.p2p.wallet.solend.ui.deposit

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import java.math.BigDecimal

interface SolendDepositContract {

    interface View : MvpView {
        fun showFullScreenLoading(isLoading: Boolean)
        fun showFeeLoading(isLoading: Boolean)

        fun showTokenToDeposit(depositToken: SolendDepositToken, withChevron: Boolean)
        fun showTokensToDeposit(depositTokens: List<SolendDepositToken>)
        fun setEmptyAmountState()
        fun setBiggerThenMaxAmountState(tokenAmount: String)
        fun setValidDepositState(output: BigDecimal, tokenAmount: String, state: SolendTransactionDetailsState)
    }

    interface Presenter : MvpPresenter<View> {
        fun initialize(userDeposits: List<SolendDepositToken>)
        fun selectTokenToDeposit(tokenToDeposit: SolendDepositToken)
        fun onTokenDepositClicked()
        fun updateInputs(input: BigDecimal, output: BigDecimal)
        fun deposit()
    }
}
