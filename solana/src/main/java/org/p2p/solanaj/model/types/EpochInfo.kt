package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class EpochInfo(
    @SerializedName("absoluteSlot")
    val absoluteSlot: Long,
    @SerializedName("blockHeight")
    val blockHeight: Long,
    @SerializedName("epoch")
    val epoch: Int,
    @SerializedName("slotIndex")
    val slotIndex: Long,
    @SerializedName("slotsInEpoch")
    val slotsInEpoch: Long,
    @SerializedName("transactionCount")
    val transactionCount: Long
)
