package org.p2p.wallet.moonpay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface BuyDetailsState : Parcelable {
    @Parcelize
    data class Valid(val data: BuyViewData) : BuyDetailsState

    @Parcelize
    data class MinAmountError(val amount: String) : BuyDetailsState
}
