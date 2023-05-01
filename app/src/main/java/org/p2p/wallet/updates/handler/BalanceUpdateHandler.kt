package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType

class BalanceUpdateHandler(
    private val gson: Gson
) : UpdateHandler {

    override suspend fun initialize() {}

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
        if (type != UpdateType.BALANCE_RECEIVED) {
            return
        }
        val jsonParsed: BalanceNotificationResponse = gson.fromJson(data, BalanceNotificationResponse::class.java)
        Timber.tag(TAG).d("Balance received, data = $jsonParsed")
    }

    companion object {
        private const val TAG = "BalanceUpdateManager"
    }
}

private data class BalanceNotificationResponse(
    @SerializedName("result")
    val result: BalanceResultResponse,
)

private data class BalanceResultResponse(
    @SerializedName("value")
    val balanceValueResponse: BalanceValueResponse,
    @SerializedName("subscription")
    val subscription: Int
)

private data class BalanceValueResponse(
    @SerializedName("lamports")
    val lamports: BigInteger,
    @SerializedName("data")
    val data: List<String>,
    @SerializedName("owner")
    val owner: String,
    @SerializedName("executable")
    val executable: Boolean,
    @SerializedName("rentEpoch")
    val rentEpoch: Int
)
