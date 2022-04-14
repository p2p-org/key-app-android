package org.p2p.wallet.push_notifications.repository

import org.p2p.wallet.push_notifications.model.DeviceToken

interface DeviceTokenRepository {
    suspend fun sendDeviceToken(deviceToken: DeviceToken): DeviceToken
    suspend fun deleteDeviceToken(deviceToken: DeviceToken): DeviceToken
}
