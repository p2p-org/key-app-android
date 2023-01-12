package org.p2p.solanaj.rpc.api

import com.google.gson.annotations.SerializedName

class RecentPerformanceSampleResponse(
    @SerializedName("numSlots")
    val numSlots: Int,
    @SerializedName("numTransactions")
    val numTransactions: Int,
    @SerializedName("samplePeriodSecs")
    val samplePeriodSecs: Int,
    @SerializedName("slot")
    val slot: Int
)
