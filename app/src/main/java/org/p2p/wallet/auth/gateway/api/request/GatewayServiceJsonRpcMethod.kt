package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

enum class GatewayServiceJsonRpcMethod {
    @SerializedName("register_wallet")
    REGISTER_WALLET,

    @SerializedName("confirm_register_wallet")
    CONFIRM_REGISTER_WALLET,

    @SerializedName("restore_wallet")
    RESTORE_WALLET,

    @SerializedName("confirm_restore_wallet")
    CONFIRM_RESTORE_WALLET,

    @SerializedName("get_metadata")
    GET_ONBOARDING_METADATA,
}
