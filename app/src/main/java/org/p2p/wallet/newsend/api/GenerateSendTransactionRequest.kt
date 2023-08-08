package org.p2p.wallet.newsend.api

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc

class GenerateSendTransactionRequest(
    val userWallet: String,
    val mintAddress: String,
    val amount: String,
    val recipient: String
) : JsonRpc<Map<String, Any>, GenerateTransactionResponse>(
    method = "transfer",
    params = buildMap {
        put("user_wallet", userWallet)
        put("mint", mintAddress)
        put("amount", amount)
        put("recipient", recipient)
    }
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<GenerateTransactionResponse>() {}.type
}
