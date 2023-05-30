package org.p2p.wallet.striga.model

import retrofit2.HttpException
import org.p2p.wallet.utils.errorBodyOrNull

sealed class StrigaDataLayerError(override val message: String) : Throwable() {
    class ApiServiceUnavailable(
        override val cause: HttpException,
        body: String = cause.errorBodyOrNull().orEmpty()
    ) : StrigaDataLayerError("Striga API unavailable, code: ${cause.code()} body: $body")

    class ApiServiceError(
        val response: StrigaApiErrorResponse
    ) : StrigaDataLayerError("Striga API returned error: code=${response.errorCode?.code} details=${response.details}")

    class InternalError(
        override val cause: Throwable? = null,
        message: String? = cause?.message,
    ) : StrigaDataLayerError(message.orEmpty())

    companion object {
        private val httpExceptionParser = StrigaDataLayerHttpErrorParser()

        fun <T> from(
            error: Throwable,
            default: StrigaDataLayerError
        ): StrigaDataLayerResult<T> {
            return when (error) {
                is HttpException -> httpExceptionParser.parse(error) ?: default
                is StrigaDataLayerError -> error
                else -> default
            }
                .toFailureResult()
        }
    }
}
