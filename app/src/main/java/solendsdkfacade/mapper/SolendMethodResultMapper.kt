package solendsdkfacade.mapper

import com.google.gson.Gson
import org.p2p.wallet.utils.fromJsonReified
import solendsdkfacade.model.SolendMethodResultError
import solendsdkfacade.model.SolendMethodResultSuccess
import timber.log.Timber

class SolendMethodResultMapper(
    // public due to inline
    val gson: Gson
) {

    inline fun <reified SuccessType : SolendMethodResultSuccess> fromSdk(sdkResult: String): SolendResult<SuccessType> {
        return SolendResult(
            success = gson.fromJsonOrNull(sdkResult),
            error = gson.fromJsonOrNull<SolendMethodResultError?>(sdkResult)
                ?.also { logMethodError(it, sdkResult) }
        )
    }

    // public due to inline
    fun logMethodError(error: SolendMethodResultError, rawError: String) {
        Timber.i(rawError)
        Timber.e(error)
    }

    inline fun <reified T> Gson.fromJsonOrNull(src: String): T? {
        return kotlin.runCatching { fromJsonReified<T>(src) }.getOrNull()
    }
}
