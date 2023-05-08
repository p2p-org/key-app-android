package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.updates.SubscriptionUpdateHandler
import org.p2p.wallet.updates.UpdateType

private const val TAG = "BalanceUpdateManager"

class SolanaAccountUpdateHandler(
    private val gson: Gson,
    private val homeLocalRepository: HomeLocalRepository
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
        if (type != UpdateType.SOL_TOKEN_UPDATED) return

        val response = gson.fromJson(data, BalanceNotificationResponse::class.java)
            .result
            .value

        updateSolToken(response)

        Timber.tag(TAG).d("Balance received, data = $data")
    }

    private suspend fun updateSolToken(response: BalanceValueResponse) {
        val cachedTokens = homeLocalRepository.getUserTokens().toMutableList()

        // faster then just map that iterates over all tokens
        val indexOfSolToken = cachedTokens.indexOfFirst(Token.Active::isSOL)
        if (indexOfSolToken == -1) return
        cachedTokens[indexOfSolToken] = cachedTokens[indexOfSolToken].let { solToken ->
            val newTotalAmount = response.lamports.fromLamports(solToken.decimals)
            val newTotalInUsd = solToken.rate?.let(newTotalAmount::times)
            solToken.copy(
                total = newTotalAmount,
                totalInUsd = newTotalInUsd,
            )
        }
        homeLocalRepository.updateTokens(cachedTokens)
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
    val rentEpoch: Int
)
