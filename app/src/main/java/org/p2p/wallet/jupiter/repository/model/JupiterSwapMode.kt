package org.p2p.wallet.jupiter.repository.model

import com.google.gson.annotations.SerializedName

enum class JupiterSwapMode {
    @SerializedName("ExactIn")
    EXACT_IN,

    @SerializedName("ExactOut")
    EXACT_OUT
}
