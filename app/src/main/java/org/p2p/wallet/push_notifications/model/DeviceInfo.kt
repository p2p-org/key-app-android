package org.p2p.wallet.push_notifications.model

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("os_name")
    val osName: String,
    @SerializedName("os_version")
    val osVersion: String,
    @SerializedName("device_model")
    val deviceModel: String
)
