package org.p2p.wallet.countrycode

import okhttp3.Response
import org.p2p.core.utils.bodyAsString

sealed class ExternalCountryCodeError(
    cause: Throwable,
    message: String? = cause.message,
) : Exception(message, cause) {

    class UnknownError(
        cause: Throwable,
        message: String? = cause.message,
    ) : ExternalCountryCodeError(cause, message)

    class EmptyResponse : ExternalCountryCodeError(
        IllegalStateException("Response body is empty")
    )

    class ParseError(cause: Throwable) : ExternalCountryCodeError(cause)

    class LocalFileReadError(message: String? = null) : ExternalCountryCodeError(
        IllegalStateException(message)
    )

    data class HttpError(
        override val message: String?,
        val response: Response
    ) : ExternalCountryCodeError(
        IllegalStateException(
            buildString {
                append(message).append("\n")
                append("[${response.code}] ${response.message} ${response.bodyAsString()}")
            }
        )
    )
}
