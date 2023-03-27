package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.api.response.BridgeSendTransactionResponse

data class TransferFromSolanaRpcRequest(
    @Transient val userWallet: SolAddress,
    @Transient val feePayer: SolAddress,
    @Transient val source: SolAddress,
    @Transient val recipient: EthAddress,
    @Transient val mint: SolAddress?,
    @Transient val amount: String,
) : JsonRpc<Map<String, Any>, BridgeSendTransactionResponse>(
    method = "transfer_from_solana",
    params = buildMap {
        put("user_wallet", userWallet)
        put("fee_payer", feePayer)
        put("from", source)
        put("recipient", recipient)
        mint?.let { put("mint", it) }
        put("amount", amount)
    }
) {
    @Transient
    override val typeOfResult: Type = BridgeSendTransactionResponse::class.java
}
