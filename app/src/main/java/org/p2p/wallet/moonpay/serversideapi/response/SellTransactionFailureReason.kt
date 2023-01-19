package org.p2p.wallet.moonpay.serversideapi.response

import com.google.gson.annotations.SerializedName

enum class SellTransactionFailureReason {
    @SerializedName("refund")
    REFUND,

    @SerializedName("timeout_deposit")
    TIMEOUT_DEPOSIT,

    @SerializedName("timeout_kyc_verification")
    TIMEOUT_KYC_VERIFICATION,

    @SerializedName("Cancelled")
    CANCELLED,

    @SerializedName("rejected_kyc")
    REJECTED_KYC,

    @SerializedName("error")
    ERROR,
}
