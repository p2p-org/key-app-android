package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

class RpcSendTransactionConfig(
    @SerializedName("encoding")
    private val encoding: Encoding
)
