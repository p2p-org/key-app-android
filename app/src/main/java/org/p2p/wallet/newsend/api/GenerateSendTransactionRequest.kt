package org.p2p.wallet.newsend.api

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.crypto.Base58String
import org.p2p.core.rpc.JsonRpc

class GenerateSendTransactionRequest(
    @SerializedName("user_wallet")
    val userWallet: Base58String,
    @SerializedName("mint")
    val mintAddress: Base58String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("recipient")
    val recipient: Base58String
) : JsonRpc<Map<String, Any>, GenerateSendTransactionResponse>(
    method = "transfer",
    params = buildMap {
        put("user_wallet", userWallet.base58Value)
        put("mint", mintAddress.base58Value)
        put("amount", amount)
        put("recipient", recipient.base58Value)
    }
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<GenerateSendTransactionResponse>() {}.type
}
