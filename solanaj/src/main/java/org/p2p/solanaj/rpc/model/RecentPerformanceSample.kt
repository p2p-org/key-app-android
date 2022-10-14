package org.p2p.solanaj.rpc.model

class RecentPerformanceSample(
    val numSlots: Int,
    val numTransactions: Int,
    val samplePeriodSecs: Int,
    val slot: Int
)
