package org.p2p.ethereumkit.internal.core.eip1559

import org.p2p.core.rpc.JsonRpc
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter

class FeeHistoryJsonRpc(
        @Transient val blocksCount: Long,
        @Transient val defaultBlockParameter: DefaultBlockParameter,
        @Transient val rewardPercentile: List<Int>,
) : JsonRpc<List<Any>, FeeHistory>(
        method = "eth_feeHistory",
        params = listOf(blocksCount, defaultBlockParameter, rewardPercentile)
) {
    @Transient
    override val typeOfResult = FeeHistory::class.java
}
