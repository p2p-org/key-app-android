package org.p2p.wallet.infrastructure.network.interceptor

import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponseType
import javax.net.ssl.HttpsURLConnection

class MoonpayRequestException(
    val httpCode: Int,
    val errorType: MoonpayErrorResponseType,
    override val message: String
) : Throwable(message) {

    val isBadRequest: Boolean = httpCode == HttpsURLConnection.HTTP_BAD_REQUEST
}
