package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsWeb3Request

class AlarmWeb3ErrorConverter(
    private val gson: Gson
) : AlarmFeatureConverter {
    fun toWeb3ErrorRequest(web3Error: String): AlarmErrorsRequest {
        // no user public key here, because we don't have it at this step
        val request = AlarmErrorsWeb3Request(
            web3Error = web3Error
        )

        return AlarmErrorsRequest(
            logsTitle = "Web3 Registration Android Alarm",
            payload = gson.toJson(request)
        )
    }
}
