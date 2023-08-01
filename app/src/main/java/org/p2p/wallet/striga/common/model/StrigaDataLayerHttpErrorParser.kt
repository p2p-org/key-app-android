package org.p2p.wallet.striga.common.model

import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.net.HttpURLConnection
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.utils.errorBodyOrNull

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
                    ?.let(StrigaDataLayerError.ApiServiceError.Companion::invoke)
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
                Timber.i("Striga returned http error: $it")
                gson.fromJsonReified<StrigaApiErrorResponse>(it)
            } ?: error("Error body is null")
        }
            .onFailure { Timber.i(it) }
            .getOrNull()
    }
}
