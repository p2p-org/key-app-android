package org.p2p.wallet.striga.model

import retrofit2.HttpException
import java.net.HttpURLConnection

sealed class StrigaDataLayerError(override val message: String) : Throwable() {
    class DatabaseError(
        override val cause: Throwable,
    ) : StrigaDataLayerError("Error while working with database: ${cause.message}")

    class MappingFailed(
        message: String
    ) : StrigaDataLayerError(message)

    class ApiServiceUnavailable(
        override val cause: HttpException
    ) : StrigaDataLayerError("Striga API unavailable, code: ${cause.code()}")

    class ApiServiceError(
        override val cause: HttpException,
        val errorBody: String
    ) : StrigaDataLayerError("Striga API returned error, code: ${cause.code()} body: $errorBody")

    class InternalError(
        override val cause: Throwable
    ) : StrigaDataLayerError(cause.message.orEmpty())

    companion object {
        fun <T> from(error: Throwable, default: StrigaDataLayerError): StrigaDataLayerResult<T> {
            return when (error) {
                is HttpException -> fromHttpException(error) ?: default
                is StrigaDataLayerError -> error
                else -> default
            }
                .toFailureResult()
        }

        private fun fromHttpException(error: HttpException): StrigaDataLayerError? {
            return when {
                error.code() == HttpURLConnection.HTTP_UNAVAILABLE -> {
                    ApiServiceUnavailable(error)
                }
                error.code() == HttpURLConnection.HTTP_SERVER_ERROR -> {
                    ApiServiceError(
                        cause = error,
                        errorBody = error.response()
                            ?.errorBody()
                            ?.string()
                            .orEmpty()
                    )
                }
                else -> {
                    null
                }
            }
        }
    }
}
