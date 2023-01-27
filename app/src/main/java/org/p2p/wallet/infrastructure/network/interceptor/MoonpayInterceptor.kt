package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponse
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponseType
import timber.log.Timber
import java.io.IOException

class MoonpayInterceptor(
    private val gson: Gson,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) response else throw extractException(response)
    }

    private fun extractException(response: Response): Throwable = try {
        val responseBody = response.body?.string().orEmpty()
        Timber.tag("Moonpay").i("Extracting exception for Moonpay: $responseBody")

        val serverError = gson.fromJson(responseBody, MoonpayErrorResponse::class.java)
        val type = MoonpayErrorResponseType.fromStringValue(serverError.type)

        MoonpayRequestException(
            httpCode = response.code,
            message = serverError.message,
            errorType = type
        ).also { Timber.i(it, "Moonpay request failed") }
    } catch (invalidResponse: JsonSyntaxException) {
        Timber.i(invalidResponse, "invalid response from Moonpay")
        MoonpayRequestException(
            httpCode = response.code,
            message = "Not valid response body for Moonpay request",
            errorType = MoonpayErrorResponseType.UNKNOWN_ERROR
        )
    } catch (e: Throwable) {
        Timber.i(e, "Error while making a request to moonpay: ${response.request.url}")
        IOException("Error reading response error body", e)
    }
}
