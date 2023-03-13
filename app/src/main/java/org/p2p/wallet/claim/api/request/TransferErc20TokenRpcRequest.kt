package org.p2p.wallet.claim.api.request

import java.lang.reflect.Type
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.claim.api.response.BridgeBundleResponse

data class TransferErc20TokenRpcRequest(
    @Transient val userEthWallet: EthAddress,
    @Transient val solRecipient: SolAddress,
    @Transient val erc20Token: EthAddress,
    @Transient val amount: String,
    @androidx.annotation.IntRange(1, 100)
    @Transient val slippage: Int
) : JsonRpc<Map<String, Any>, BridgeBundleResponse>(
    method = "transfer_tokens",
    params = buildMap {
        put("user_wallet", userEthWallet)
        put("recipient", solRecipient)
        put("token", erc20Token)
        put("amount", amount)
        put("slippage", slippage)
    }
) {
    @Transient
    override val typeOfResult: Type = BridgeBundleResponse::class.java
}
