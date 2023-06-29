package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.wallet.alarmlogger.api.AlarmErrorsDeviceShareChangeRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.DeviceShareChangeErrorRequest
import org.p2p.wallet.utils.Base58String

class AlarmDeviceShareChangeErrorConverter(
    private val gson: Gson
) {

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
