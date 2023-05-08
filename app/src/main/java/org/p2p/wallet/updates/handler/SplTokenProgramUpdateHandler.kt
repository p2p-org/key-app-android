package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.utils.fromLamports
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.updates.SubscriptionUpdateHandler
import org.p2p.wallet.updates.UpdateType

private const val TAG = "TokensBalanceUpdateManager"

class SplTokenProgramUpdateHandler(
    private val gson: Gson,
    private val tokenRepository: HomeLocalRepository
) : SubscriptionUpdateHandler {

    override suspend fun initialize() = Unit

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
        if (type != UpdateType.SPL_TOKEN_PROGRAM_UPDATED) return

        val response = gson.fromJson(data, TokenProgramNotificationResponse::class.java).result.value

        val programData = response.account.data?.firstOrNull()?.toBase64Instance() ?: return
        val updatedTokenData = TokenProgram.AccountInfoData.decode(programData.decodeToBytes())
        updateToken(updatedTokenData)

        Timber.tag(TAG).d("SPL Token data received, data = $data")
    }

    private suspend fun updateToken(newTokenData: TokenProgram.AccountInfoData) {
        val cachedTokens = tokenRepository.getUserTokens().toMutableList()
        val indexOfTokenToUpdate = cachedTokens.indexOfFirst { it.mintAddress == newTokenData.mint.toBase58() }
        if (indexOfTokenToUpdate == -1) return

        cachedTokens[indexOfTokenToUpdate] = cachedTokens[indexOfTokenToUpdate].let { token ->
            val newTotalAmount = newTokenData.amount.fromLamports(token.decimals)
            val newTotalInUsd = token.rate?.let(newTotalAmount::times)
            token.copy(
                total = newTotalAmount,
                totalInUsd = newTotalInUsd,
            )
        }

        tokenRepository.updateTokens(cachedTokens)
    }
}

private data class TokenProgramNotificationResponse(
    @SerializedName("result")
    val result: RpcNotificationResultResponse<TokenNotificationResponse>,
)

private data class TokenNotificationResponse(
    @SerializedName("pubkey")
    val publicKey: String,
    @SerializedName("account")
    val account: AccountNotificationResponse
)

private data class AccountNotificationResponse(
    @SerializedName("lamports")
    val lamports: BigInteger,
    @SerializedName("data")
    val data: List<String>? = null,
    @SerializedName("owner")
    val owner: String,
    @SerializedName("executable")
    val executable: Boolean,
    @SerializedName("rentEpoch")
    val rentEpoch: Int
)
