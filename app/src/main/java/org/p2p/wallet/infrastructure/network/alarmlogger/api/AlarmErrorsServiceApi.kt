package org.p2p.wallet.infrastructure.network.alarmlogger.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AlarmErrorsServiceApi {
    @POST("integrations/v1/formatted_webhook/yQ9zMIbgg64nhdKC1TAViG53t/")
    suspend fun sendAlarm(@Body request: AlarmErrorsRequest)
}
