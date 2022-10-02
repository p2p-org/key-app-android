package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.Gson
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class SolendMethodResultMapper(
    // public due to inline
    val gson: Gson
) {

    inline fun <reified ResultT> fromSdk(sdkResult: String): ResultT {
        val mapperResponse = gson.fromJsonReified<SolendResult<ResultT>>(sdkResult)

        logMethodResponse(mapperResponse, sdkResult)

        if (mapperResponse?.error != null) {
            Timber.e(mapperResponse.error)
            throw mapperResponse.error
        }

        return mapperResponse?.success ?: error("Failed to map result from sdk: $sdkResult")
    }

    fun <ResultT> logMethodResponse(mapperResponse: SolendResult<ResultT>?, sdkResult: String) {
        val logMessage = buildString {
            if (mapperResponse?.error != null) {
                append("ERROR")
            } else {
                append("SUCCESS")
            }
            append(" -------> ")
            appendLine()
            append(sdkResult)
        }
        Timber.tag("SolendMethodResultMapper").d(logMessage)
    }
}
