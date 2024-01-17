package org.p2p.wallet.send.model.send_service

import com.google.gson.annotations.SerializedName

enum class SendTransferMode {
    /**
     * Recipient receives the exact amount of tokens specified in the transaction.
     */
    @SerializedName("ExactOut")
    ExactOut,

    /**
     * Sender sends the exact amount of tokens specified in the transaction.
     */
    @SerializedName("ExactIn")
    ExactIn,
}
