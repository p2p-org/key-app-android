package org.p2p.wallet.claim.ui

import java.math.BigDecimal
import org.p2p.wallet.claim.model.ClaimDetails
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ClaimContract {

    interface View : MvpView {
        fun setTitle(title: String)
        fun setTokenIconUrl(tokenIconUrl: String)
        fun setTokenAmount(tokenAmount: String)
        fun setFiatAmount(fiatAmount: String)
        fun showFee(fee: String)
        fun showClaimFeeInfo(claimDetails: ClaimDetails)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData(
            tokenSymbol: String,
            tokenAmount: BigDecimal,
            fiatAmount: BigDecimal
        )
        fun onFeeClicked()
        fun onSendButtonClicked()
    }
}
