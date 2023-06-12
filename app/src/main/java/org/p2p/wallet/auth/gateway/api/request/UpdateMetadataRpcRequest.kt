package org.p2p.wallet.auth.gateway.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.auth.gateway.api.response.UpdateMetadataResponse
import org.p2p.wallet.utils.Base58String

data class UpdateMetadataRpcRequest(
    @Transient val ethereumAddress: String,
    @Transient val userPublicKey: Base58String,
    @Transient val encryptedMetadata: Base64String,
    @Transient val timestamp: String,
    @Transient val requestSignature: String,
) : JsonRpc<Map<String, Any>, UpdateMetadataResponse>(
    method = "update_metadata",
    params = buildMap {
        put("ethereum_id", ethereumAddress)
        put("solana_pubkey", userPublicKey)
        put("metadata", encryptedMetadata)
        put("timestamp_device", timestamp)
        put("signature", requestSignature)
    }
) {
    @Transient
    override val typeOfResult: Type = UpdateMetadataResponse::class.java
}
