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
                    ?.let(StrigaDataLayerError.ApiServiceError::invoke)
                    ?: StrigaDataLayerError.InternalError(cause = httpException)
            }
            else -> {
                null
            }
        }

    private fun parseJsonErrorBody(response: Response<*>): StrigaApiErrorResponse? {
        return response.runCatching {
            val errorBody = response.errorBodyOrNull()
            errorBody?.let {
                val body = if (it.contains("\\\"")) {
                    Timber.d("Error body contains escaped quotes, replacing them")
                    val fixedBody = it.replace("\\\"", "\"")
                    fixedBody.substring(1).take(fixedBody.length - 2)
                } else {
                    it
                }
                gson.fromJsonReified<StrigaApiErrorResponse>(body)
            } ?: error("Error body is null")
        }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }
}
