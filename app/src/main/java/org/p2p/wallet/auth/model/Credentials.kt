package org.p2p.wallet.auth.model

import com.google.gson.annotations.SerializedName

data class Credentials(
    @SerializedName("geetest_validate")
    val geeTestValidate: String,
    @SerializedName("geetest_seccode")
    val geeTestSecCode: String,
    @SerializedName("geetest_challenge")
    val geeTestChallenge: String
)
