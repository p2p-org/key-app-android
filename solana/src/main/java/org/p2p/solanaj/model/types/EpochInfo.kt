package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class EpochInfo(
    @SerializedName("absoluteSlot")
    val absoluteSlot: BigInteger,
    @SerializedName("blockHeight")
    val blockHeight: BigInteger,
    @SerializedName("epoch")
    val epoch: BigInteger,
    @SerializedName("slotIndex")
    val slotIndex: BigInteger,
    @SerializedName("slotsInEpoch")
    val slotsInEpoch: BigInteger,
    @SerializedName("transactionCount")
    val transactionCount: Long
)
