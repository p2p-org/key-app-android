package org.p2p.wallet.push_notifications.repository

import org.p2p.solanaj.model.types.RpcRequest2
import org.p2p.wallet.push_notifications.api.NotificationServiceApi
import org.p2p.wallet.push_notifications.model.DeviceToken

private const val METHOD_ADD_DEVICE = "add_device"
private const val METHOD_DELETE_DEVICE = "delete_device"

class DeviceTokenRemoteRepository(
    private val api: NotificationServiceApi
) : DeviceTokenRepository {

    override suspend fun sendDeviceToken(params: Map<String, Any>): DeviceToken {
        val request = RpcRequest2(
            method = METHOD_ADD_DEVICE,
            params = params
        )

        return api.sendDeviceToken(request).result
    }

    override suspend fun deleteDeviceToken(params: Map<String, Any>): DeviceToken {
        val request = RpcRequest2(
            method = METHOD_DELETE_DEVICE,
            params
        )

        return api.deleteDeviceToken(request).result
    }
}
