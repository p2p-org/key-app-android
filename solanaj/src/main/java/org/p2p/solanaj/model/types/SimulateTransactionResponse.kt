package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class SimulateTransactionResponse(
    @SerializedName("err")
    val error: String?,
    @SerializedName("accounts")
    val accounts: String?,
    @SerializedName("logs")
    val logs: List<String>?
)