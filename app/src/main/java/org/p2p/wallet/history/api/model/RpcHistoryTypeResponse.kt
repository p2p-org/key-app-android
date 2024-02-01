package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

enum class RpcHistoryTypeResponse {
    @SerializedName("send")
    SEND,

    @SerializedName("receive")
    RECEIVE,

    @SerializedName("referral_reward")
    REFERRAL_REWARD,

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

    @SerializedName("wormhole_receive")
    WORMHOLE_RECEIVE,

    @SerializedName("wormhole_send")
    WORMHOLE_SEND,

    @SerializedName("unknown")
    UNKNOWN
}
