package org.p2p.wallet.push_notifications.repository

import org.p2p.wallet.push_notifications.model.DeviceToken

interface DeviceTokenRepository {
    suspend fun sendDeviceToken(params: Map<String, Any>): DeviceToken
    suspend fun deleteDeviceToken(params: Map<String, Any>): DeviceToken
}
