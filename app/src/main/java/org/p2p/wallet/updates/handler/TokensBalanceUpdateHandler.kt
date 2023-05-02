package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.math.BigInteger
import org.p2p.solanaj.model.types.RpcNotificationResultResponse
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType

class TokensBalanceUpdateHandler(
    private val gson: Gson
) : UpdateHandler {

    override suspend fun initialize() {}

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
        if (type != UpdateType.TOKEN_BALANCES_RECEIVED) {
            return
        }
        val jsonParsed: TokenProgramNotificationResponse =
            gson.fromJson(data, TokenProgramNotificationResponse::class.java)

        Timber.tag(TAG).d("Tokens balance received, data = $data")
    }

    companion object {
        const val PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        private const val TAG = "TokensBalanceUpdateManager"
    }
}

private data class TokenProgramNotificationResponse(
    @SerializedName("result")
    val result: RpcNotificationResultResponse<TokenNotificationResponse>,
)

private data class TokenNotificationResponse(
    @SerializedName("pubkey")
    val pubkey: String,
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
