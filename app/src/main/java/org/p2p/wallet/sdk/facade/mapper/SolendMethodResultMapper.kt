package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.Gson
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class SolendMethodResultMapper(
    // public due to inline
    val gson: Gson
) {

    inline fun <reified ResultT : SolendMethodResultSuccess> fromSdk(sdkResult: String): SolendResult<ResultT> {
        return gson.fromJsonReified<SolendResult<ResultT>>(sdkResult)
            ?.onErrorLog(sdkResult)
            ?: error("Failed to map result from sdk: $sdkResult")
    }

    // public due to inline
    fun <ResultType : SolendMethodResultSuccess> SolendResult<ResultType>.onErrorLog(
        rawSdkResult: String
    ): SolendResult<ResultType> {
        if (error != null) {
            Timber.i(rawSdkResult)
            Timber.e(error)
        }
        return this
    }
}
