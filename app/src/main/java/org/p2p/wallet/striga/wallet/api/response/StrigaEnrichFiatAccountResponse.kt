package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaEnrichFiatAccountResponse(
    @SerializedName("currency")
    val currency: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("internalAccountId")
    val internalAccountId: String,
    @SerializedName("bankCountry")
    val bankCountry: String,
    @SerializedName("bankAddress")
    val bankAddress: String,
    @SerializedName("iban")
    val iban: String,
    @SerializedName("bic")
    val bic: String,
    @SerializedName("accountNumber")
    val accountNumber: String,
    @SerializedName("bankName")
    val bankName: String,
    @SerializedName("bankAccountHolderName")
    val bankAccountHolderName: String,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("paymentType")
    val paymentType: String?,
    @SerializedName("domestic")
    val domestic: Boolean,
    @SerializedName("routingCodeEntries")
    val routingCodeEntries: List<String>,
    @SerializedName("payInReference")
    val payInReference: String?,
)
