package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SimulateTransactionResponse(
    @SerializedName("value")
    val value: SimulateTransactionDetailsResponse
)

data class SimulateTransactionDetailsResponse(
    @SerializedName("err")
    val error: JSONObject?,
    @SerializedName("accounts")
    val accounts: String?,
    @SerializedName("logs")
    val logs: List<String>?
) {

    fun linedLogs(): String = buildString {
        logs?.forEach {
            append(it)
            append("\n")
        }
    }
}
