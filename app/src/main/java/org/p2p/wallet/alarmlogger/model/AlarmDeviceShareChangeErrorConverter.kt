package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.alarmlogger.api.AlarmErrorsDeviceShareChangeRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.DeviceShareChangeErrorRequest

class AlarmDeviceShareChangeErrorConverter(
    private val gson: Gson
) : AlarmFeatureConverter {

    fun toDeviceShareChangeErrorRequest(
        userPublicKey: Base58String,
        error: DeviceShareChangeAlarmError
    ): AlarmErrorsRequest {
        val throwable = error.error
        val request = AlarmErrorsDeviceShareChangeRequest(
            userPublicKey = userPublicKey,
            deviceShareChangeError = DeviceShareChangeErrorRequest(
                source = error.source,
                error = throwable.message ?: throwable.localizedMessage ?: "Unknown error"
            )
        )
        return AlarmErrorsRequest(
            logsTitle = "Device share changes Android Alarm",
            payload = gson.toJson(request)
        )
    }
}
