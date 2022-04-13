package org.p2p.wallet.push_notifications.api

import org.p2p.solanaj.model.types.RpcRequest2
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface NotificationServiceApi {

    @POST
    suspend fun sendDeviceToken(
        @Body rpcRequest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<DeviceToken>

    @POST
    suspend fun deleteDeviceToken(
        @Body rpcRequest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<DeviceToken>
}
