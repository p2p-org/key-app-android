package org.p2p.wallet.claim.api.request

import java.lang.reflect.Type
import java.util.Optional
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.claim.api.response.BundleFeeResponse

class GetClaimFeeRpcRequest(
    @Transient val ethWallet: EthAddress,
    @Transient val solRecipient: SolAddress,
    @Transient val erc20Token: Optional<EthAddress>,
    @Transient val amount: String
) : JsonRpc<Map<String, Any>, BundleFeeResponse>(
    method = "get_claim_fees",
    params = buildMap {
        put("user_wallet", ethWallet)
        put("recipient", solRecipient)
        put("token", erc20Token)
        put("amount", amount)
    }
) {
    override val typeOfResult: Type = BundleFeeResponse::class.java
}
