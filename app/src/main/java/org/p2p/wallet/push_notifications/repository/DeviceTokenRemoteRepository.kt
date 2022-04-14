package org.p2p.wallet.push_notifications.repository

import org.p2p.solanaj.model.types.RpcRequestObject
import org.p2p.wallet.push_notifications.api.NotificationServiceApi
import org.p2p.wallet.push_notifications.model.DeviceToken

private const val METHOD_ADD_DEVICE = "add_device"
private const val METHOD_DELETE_DEVICE = "delete_device"

class DeviceTokenRemoteRepository(
    private val api: NotificationServiceApi
) : DeviceTokenRepository {

    override suspend fun sendDeviceToken(deviceToken: DeviceToken): DeviceToken {
        val request = RpcRequestObject(
            method = METHOD_ADD_DEVICE,
            params = deviceToken
        )

        return api.sendDeviceToken(request).result
    }

    override suspend fun deleteDeviceToken(deviceToken: DeviceToken): DeviceToken {
        val request = RpcRequestObject(
            method = METHOD_DELETE_DEVICE,
            params = deviceToken
        )

        return api.deleteDeviceToken(request).result
    }
}
