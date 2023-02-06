package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.annotations.SerializedName

class KeyAppSdkResult<SuccessType>(
    @SerializedName("success")
    val success: SuccessType? = null,
    @SerializedName("error")
    val error: String? = null
)
