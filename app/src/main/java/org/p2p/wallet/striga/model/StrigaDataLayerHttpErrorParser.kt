package org.p2p.wallet.striga.model

import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import org.p2p.wallet.utils.errorBodyOrNull
import org.p2p.wallet.utils.fromJsonReified

class StrigaDataLayerHttpErrorParser {
    private val gson: Gson = Gson()

    fun parse(httpException: HttpException): StrigaDataLayerError? =
        when (httpException.code()) {
            HttpURLConnection.HTTP_UNAVAILABLE, HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                StrigaDataLayerError.ApiServiceUnavailable(httpException)
            }
            HttpURLConnection.HTTP_CONFLICT, HttpURLConnection.HTTP_BAD_REQUEST -> {
                httpException.response()
                    ?.let(::parseJsonErrorBody)
                    ?.let(StrigaDataLayerError::ApiServiceError)
                    ?: StrigaDataLayerError.InternalError(cause = httpException)
            }
            else -> {
                null
            }
        }

    private fun parseJsonErrorBody(response: Response<*>): StrigaApiErrorResponse? {
        return response.runCatching {
            errorBodyOrNull()?.let { gson.fromJsonReified<StrigaApiErrorResponse>(it) }
        }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }
}
