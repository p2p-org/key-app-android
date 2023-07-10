package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.core.crypto.toBase64Instance
import org.p2p.wallet.home.repository.UserTokensRepository
import org.p2p.wallet.updates.SocketSubscriptionUpdateType
import org.p2p.wallet.updates.SubscriptionUpdateHandler
import org.p2p.core.crypto.toBase58Instance

private const val TAG = "TokensBalanceUpdateManager"

class SplTokenProgramUpdateHandler(
    private val gson: Gson,
    private val tokensRepository: UserTokensRepository
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: SocketSubscriptionUpdateType, data: JsonObject) {
        if (type != SocketSubscriptionUpdateType.SPL_TOKEN_PROGRAM_UPDATED) return

        val response = gson.fromJson(data, TokenProgramNotificationResponse::class.java).result.value

        val programData = response.account.data?.firstOrNull()?.toBase64Instance() ?: return
        val updatedTokenData = TokenProgram.AccountInfoData.decode(programData.decodeToBytes())
        tokensRepository.updateUserToken(
            newBalanceLamports = updatedTokenData.amount,
            tokenMint = updatedTokenData.mint.toBase58().toBase58Instance(),
            accountPublicKey = response.accountPublicKey.toBase58Instance()
        )
        Timber.tag(TAG).d(
            "SPL Token data received, " +
                "amountInLamports=${updatedTokenData.amount}, mint=${updatedTokenData.mint}, data = $data"
        )
    }
}

private data class TokenProgramNotificationResponse(
    @SerializedName("result")
    val result: RpcNotificationResultResponse<TokenNotificationResponse>,
)

private data class TokenNotificationResponse(
    @SerializedName("pubkey")
    val accountPublicKey: String,
    @SerializedName("account")
    val account: AccountNotificationResponse
)

private data class AccountNotificationResponse(
    @SerializedName("lamports")
    val lamports: BigInteger,
    @SerializedName("data")
    val data: List<String>? = null,
    @SerializedName("owner")
    val programPublicKey: String,
    @SerializedName("executable")
    val executable: Boolean,
    @SerializedName("rentEpoch")
    val rentEpoch: Int
)
