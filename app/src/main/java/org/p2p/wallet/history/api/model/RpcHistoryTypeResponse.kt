package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

enum class RpcHistoryTypeResponse {
    @SerializedName("send")
    SEND,
    @SerializedName("receive")
    RECEIVE,
    @SerializedName("swap")
    SWAP,
    @SerializedName("stake")
    STAKE,
    @SerializedName("unstake")
    UNSTAKE,
    @SerializedName("create_account")
    CREATE_ACCOUNT,
    @SerializedName("close_account")
    CLOSE_ACCOUNT,
    @SerializedName("mint")
    MINT,
    @SerializedName("burn")
    BURN,
    @SerializedName("unknown")
    UNKNOWN
}
