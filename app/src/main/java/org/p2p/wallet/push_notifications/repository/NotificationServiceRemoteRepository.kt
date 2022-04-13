package org.p2p.wallet.push_notifications.repository

import org.p2p.solanaj.model.types.RpcRequest2
import org.p2p.wallet.push_notifications.api.NotificationServiceApi
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.utils.emptyString

class NotificationServiceRemoteRepository(
    private val api: NotificationServiceApi
) : NotificationServiceRepository {

    override suspend fun sendDeviceToken(params: Map<String, Any>): DeviceToken {
        val request = RpcRequest2(
            method = emptyString(),
            params = params
        )
        return api.sendDeviceToken(request).result
    }

    override suspend fun deleteDeviceToken(params: Map<String, Any>): DeviceToken {
        val request = RpcRequest2(
            method = emptyString(),
            params
        )

        return api.deleteDeviceToken(request).result
    }
}
