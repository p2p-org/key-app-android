package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaInitEurOffRampRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("sourceAccountId")
    val sourceAccountId: String,
    @SerializedName("amount")
    val amountInUnits: String,
    @SerializedName("destination")
    val destination: BankingDetailsRequest
) {
    class BankingDetailsRequest(
        @SerializedName("iban")
        val iban: String,
        @SerializedName("bic")
        val bic: String,
    )
}
