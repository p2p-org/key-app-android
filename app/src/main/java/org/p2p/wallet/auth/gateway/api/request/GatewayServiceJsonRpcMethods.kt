package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

enum class GatewayServiceJsonRpcMethods {
    @SerializedName("register_wallet")
    REGISTER_WALLET,

    @SerializedName("Confirm_register_wallet")
    CONFIRM_REGISTER_WALLET
}
