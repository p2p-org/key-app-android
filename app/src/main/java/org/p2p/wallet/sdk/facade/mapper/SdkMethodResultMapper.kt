package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.p2p.wallet.sdk.facade.model.KeyAppSdkMethodResultException
import timber.log.Timber

class SdkMethodResultMapper(
    // public due to inline
    val gson: Gson
) {
    inline fun <reified ResultT> fromSdk(sdkResult: String): ResultT {
        val type = object : TypeToken<KeyAppSdkResult<ResultT>>() {}.type
        val mapperResponse = gson.fromJson<KeyAppSdkResult<ResultT>>(sdkResult, type)

        if (mapperResponse?.error != null) {
            val exception = KeyAppSdkMethodResultException(mapperResponse.error)
            Timber.e(exception)
            throw exception
        }

        return mapperResponse?.success ?: error("Failed to map result from sdk: $sdkResult")
    }
}
