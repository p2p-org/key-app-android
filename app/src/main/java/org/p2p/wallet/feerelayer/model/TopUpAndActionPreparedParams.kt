package org.p2p.wallet.feerelayer.model

@Deprecated("Old swap")
class TopUpAndActionPreparedParams(
    val topUpPreparedParam: TopUpPreparedParams?,
    val actionFeesAndPools: FeesAndPools
)
