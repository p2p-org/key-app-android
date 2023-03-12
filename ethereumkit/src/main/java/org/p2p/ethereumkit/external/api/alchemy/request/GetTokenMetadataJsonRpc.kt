package org.p2p.ethereumkit.external.api.alchemy.request

import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.EthAddress
import java.lang.reflect.Type

internal class GetTokenMetadataJsonRpc(
    @Transient val contractAddresses: EthAddress
): JsonRpc<List<Any>,TokenMetadataResponse>(
    method = "alchemy_getTokenMetadata",
    params = listOf(contractAddresses)
) {
    @Transient
    override val typeOfResult: Type = TokenMetadataResponse::class.java
}
