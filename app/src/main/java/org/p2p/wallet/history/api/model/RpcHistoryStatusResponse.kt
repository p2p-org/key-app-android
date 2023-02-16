package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

enum class RpcHistoryStatusResponse {
    @SerializedName("success")
    SUCCESS,

    @SerializedName("fail")
    FAIL
}
