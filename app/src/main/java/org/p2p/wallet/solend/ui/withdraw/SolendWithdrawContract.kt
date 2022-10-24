package org.p2p.wallet.solend.ui.withdraw

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import java.math.BigDecimal

interface SolendWithdrawContract {

    interface View : MvpView {
        fun showFullScreenLoading(isLoading: Boolean)
        fun showFeeLoading(isLoading: Boolean)

        fun showTokenToWithdraw(depositToken: SolendDepositToken.Active, withChevron: Boolean)
        fun showTokensToWithdraw(depositTokens: List<SolendDepositToken>)
        fun setEmptyAmountState()
        fun setBiggerThenMaxAmountState(tokenAmount: String)
        fun setValidDepositState(
            output: BigDecimal,
            tokenAmount:
                String,
            state:
                SolendTransactionDetailsState.Withdraw
        )

        fun showIndefiniteInfoMessage(@StringRes messageRes: Int, @StringRes actionButtonRes: Int)

        fun navigateToEarnScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun initialize(userDeposits: List<SolendDepositToken>)
        fun selectTokenToWithdraw(tokenToWithdraw: SolendDepositToken.Active)
        fun onTokenWithdrawClicked()
        fun updateInputs(input: BigDecimal, output: BigDecimal)
        fun withdraw()
    }
}
