package org.p2p.wallet.auth.username.api.request

import com.google.gson.annotations.SerializedName

enum class RegisterUsernameServiceJsonRpcMethod {
    @SerializedName("get_name")
    GET_NAME,

    @SerializedName("create_name")
    CREATE_NAME,

    @SerializedName("delete_name")
    DELETE_NAME,

    @SerializedName("resolve_name")
    RESOLVE_NAME,

    @SerializedName("lookup_name")
    LOOKUP_NAME,

    @SerializedName("authenticate")
    AUTHENTICATE
}
