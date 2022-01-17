package org.p2p.wallet.rpc.api

import com.google.gson.annotations.SerializedName

data class RequestInstruction(
    @SerializedName("program_id")
    val programIdIndex: Int,
    @SerializedName("accounts")
    val accounts: List<RequestAccountMeta>,
    @SerializedName("data")
    val data: List<Int>
)