package org.p2p.wallet.moonpay.model

import org.p2p.wallet.infrastructure.network.interceptor.MoonpayRequestException

sealed class MoonpaySellError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable() {
    class NotEnoughTokenToSell(cause: Throwable) :
        MoonpaySellError(message = "Not enough token to sell", cause = cause)

    class UnauthorizedRequest(cause: Throwable) :
        MoonpaySellError(message = "Request was unauthorized", cause = cause)

    class NoInternetForRequest(cause: Throwable) :
        MoonpaySellError(message = "No internet for making request", cause = cause)

    class UnknownError(cause: Throwable) :
        MoonpaySellError(message = "Unexpected error from Moonpay", cause = cause)

    class TokenToSellNotFound(cause: Throwable) :
        MoonpaySellError(message = "Cryptocurrency to sell not found", cause = cause)

    class RequestFailed(cause: MoonpayRequestException) :
        MoonpaySellError(message = cause.message, cause = cause)
}
