package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.wallet.alarmlogger.api.AlarmErrorStrigaRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.StrigaErrorRequest
import org.p2p.wallet.utils.Base58String

class StrigaErrorConverter(
    private val gson: Gson
) {

    fun toStrigaErrorRequest(userPublicKey: Base58String, error: StrigaAlarmError): AlarmErrorsRequest {
        val request = AlarmErrorStrigaRequest(
            userPublicKey = userPublicKey,
            strigaError = StrigaErrorRequest(
                source = error.source,
                kycSdkState = error.kycSdkState,
                error = error.error
            )
        )
        return AlarmErrorsRequest(
            logsTitle = "Striga Registration Android Alarm",
            payload = gson.toJson(request)
        )
    }
}
