package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.p2p.wallet.sdk.facade.model.SolendMethodResultException
import timber.log.Timber

class SolendMethodResultMapper(
    // public due to inline
    val gson: Gson
) {

    inline fun <reified ResultT> fromSdk(sdkResult: String): ResultT {
        Timber.tag("SolendMethodResultMapper fromSdk: ").d(sdkResult)

        val type = object : TypeToken<SolendResult<ResultT>>() {}.type
        val mapperResponse = gson.fromJson<SolendResult<ResultT>>(sdkResult, type)

        logMethodResponse(mapperResponse, sdkResult)

        if (mapperResponse?.error != null) {
            val exception = SolendMethodResultException(mapperResponse.error)
            Timber.e(exception)
            throw exception
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
