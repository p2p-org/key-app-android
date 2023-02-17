package org.p2p.wallet.moonpay.serversideapi.response

import com.google.gson.annotations.SerializedName

enum class SellTransactionStatus {
    @SerializedName("waitingForDeposit")
    WAITING_FOR_DEPOSIT,

    @SerializedName("pending")
    PENDING,

    @SerializedName("failed")

    FAILED,

    @SerializedName("completed")
    COMPLETED,
}
