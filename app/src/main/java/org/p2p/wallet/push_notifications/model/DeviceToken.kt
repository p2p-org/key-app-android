package org.p2p.wallet.push_notifications.model

import com.google.gson.annotations.SerializedName

data class DeviceToken(
    @SerializedName("device_token")
    val deviceToken: String,
    @SerializedName("device_info")
    val deviceInfo: DeviceInfo,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("timestamp")
    val timestamp: String
)
