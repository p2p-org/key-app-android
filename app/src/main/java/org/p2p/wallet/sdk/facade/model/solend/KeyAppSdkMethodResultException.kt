package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

data class KeyAppSdkMethodResultException(
    @SerializedName("error")
    val error: String,
) : Throwable(message = "Solend method failed: $error")
