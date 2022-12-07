package org.p2p.solanaj.kits.transaction.network.transaction

import com.google.gson.annotations.SerializedName

data class TransactionInformationResponse(
    @SerializedName("message")
    val message: MessageResponse,

    @SerializedName("signatures")
    val signatures: List<String>
) {
    fun getTransactionId(): String = signatures.first()
}
