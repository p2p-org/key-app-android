package org.p2p.wallet.auth.username.api.request

import com.google.gson.annotations.SerializedName

class GetNameRequest(
    @SerializedName("name")
    val name: String
)
