package org.p2p.wallet.moonpay.repository.sell

import okio.IOException
import org.p2p.core.network.data.ServerException
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponseType
import org.p2p.wallet.moonpay.model.MoonpaySellError
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SellRepositoryErrorMapper {
    // add more errors if needed
    fun fromNetworkError(error: Throwable): MoonpaySellError = when (error) {
        is ServerException -> {
            val moonpayErrorType = error.jsonErrorBody
                ?.getAsJsonPrimitive("type")
                ?.asString

            val moonpayErrorMessage = error.jsonErrorBody
                ?.getAsJsonPrimitive("message")
                ?.asString
                .orEmpty()
            when {
                moonpayErrorType == MoonpayErrorResponseType.NOT_FOUND_ERROR.stringValue -> {
                    MoonpaySellError.TokenToSellNotFound(error)
                }
                moonpayErrorType == MoonpayErrorResponseType.BAD_REQUEST_ERROR.stringValue &&
                    moonpayErrorMessage.contains("The minimum order amount") -> {
                    MoonpaySellError.NotEnoughTokenToSell(error)
                }
                else -> {
                    MoonpaySellError.UnknownError(error)
                }
            }
        }
        is UnknownHostException, is SocketTimeoutException -> {
            MoonpaySellError.NoInternetForRequest(error)
        }
        is IllegalStateException, is IOException -> {
            MoonpaySellError.UnknownError(error)
        }
        else -> {
            MoonpaySellError.UnknownError(error)
        }
    }
}
