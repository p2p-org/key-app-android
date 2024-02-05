package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.wallet.updates.SocketSubscriptionUpdateType
import org.p2p.wallet.updates.SubscriptionUpdateHandler
import org.p2p.wallet.user.interactor.UserTokensInteractor

private const val TAG = "BalanceUpdateManager"

class SolanaAccountUpdateHandler(
    private val gson: Gson,
    private val userTokensInteractor: UserTokensInteractor
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: SocketSubscriptionUpdateType, data: JsonObject) {
        if (type != SocketSubscriptionUpdateType.SOL_TOKEN_UPDATED) return

        val response = gson.fromJson(data, BalanceNotificationResponse::class.java)
            .result
            .value

        userTokensInteractor.updateSolanaBalance(newBalanceInLamports = response.lamports)

        Timber.tag(TAG).d("SOL balance updated, new amount: ${response.lamports}, data = $data")
    }
}

private data class BalanceNotificationResponse(
    @SerializedName("result")
    val result: RpcNotificationResultResponse<BalanceValueResponse>,
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
    val rentEpoch: BigInteger
)
