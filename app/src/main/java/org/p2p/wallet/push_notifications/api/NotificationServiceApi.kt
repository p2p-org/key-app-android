package org.p2p.wallet.push_notifications.api

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.push_notifications.model.DeviceToken
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationServiceApi {

    @POST("/")
    suspend fun sendDeviceToken(
        @Body rpcRequest: RpcRequest
    ): CommonResponse<DeviceToken>

    @POST("/")
    suspend fun deleteDeviceToken(
        @Body rpcRequest: RpcRequest
    ): CommonResponse<DeviceToken>
}
