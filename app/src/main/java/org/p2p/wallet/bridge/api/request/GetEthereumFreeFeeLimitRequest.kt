package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import java.math.BigDecimal
import org.p2p.core.rpc.JsonRpc

object GetEthereumFreeFeeLimitRequest : JsonRpc<Map<String, Any>, BigDecimal>(
    method = "get_ethereum_free_fee_limit",
    params = hashMapOf()
) {
    @Transient
    override val typeOfResult: Type = BigDecimal::class.java
}
