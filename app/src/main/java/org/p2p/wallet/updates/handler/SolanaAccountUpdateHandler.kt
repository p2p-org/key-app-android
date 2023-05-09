package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.utils.Constants
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.wallet.home.repository.UserTokensRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.updates.SubscriptionUpdateHandler
import org.p2p.wallet.updates.UpdateType
import org.p2p.wallet.utils.toBase58Instance

private const val TAG = "BalanceUpdateManager"

class SolanaAccountUpdateHandler(
    private val gson: Gson,
    private val tokensRepository: UserTokensRepository,
    private val tokenKeyProvider: TokenKeyProvider
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
        if (type != UpdateType.SOL_TOKEN_UPDATED) return

        val response = gson.fromJson(data, BalanceNotificationResponse::class.java)
            .result
            .value

        tokensRepository.updateUserToken(
            newBalanceLamports = response.lamports,
            tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance(),
            accountPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )

        Timber.tag(TAG).d("New sol balance received, data = $data")
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
