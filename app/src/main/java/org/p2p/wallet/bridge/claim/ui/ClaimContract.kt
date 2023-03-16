package org.p2p.wallet.bridge.claim.ui

import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ClaimContract {

    interface View : MvpView {
        fun setTitle(title: String)
        fun setTokenIconUrl(tokenIconUrl: String?)
        fun setTokenAmount(tokenAmount: String)
        fun setFiatAmount(fiatAmount: String)
        fun showFee(fee: String)
        fun showClaimFeeInfo(claimDetails: ClaimDetails)
        fun showClaimButtonValue(tokenAmountToClaim: String)
        fun setClaimButtonState(isButtonEnabled: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFeeClicked()
        fun onSendButtonClicked()
    }
}
