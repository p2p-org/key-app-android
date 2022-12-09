package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.feerelayer.ErrorTypeConverter
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponse
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponseType
import timber.log.Timber
import java.io.IOException

class MoonpayErrorInterceptor(private val gson: Gson) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            response
        } else {
            throw extractException(response)
        }
    }

    private fun extractException(response: Response): Throwable = try {
        val responseBody = response.body?.string()!!
        Timber.tag("Moonpay").i("Extracting exception: $responseBody")

        val serverError = gson.fromJson(responseBody, MoonpayErrorResponse::class.java)
        val type = MoonpayErrorResponseType.fromStringValue(serverError.type)
        val errorCode = ErrorTypeConverter.fromMoonpay(type)

        ServerException(
            errorCode = errorCode,
            fullMessage = serverError.message,
            errorMessage = serverError.message
        )
    } catch (e: Throwable) {
        Timber.i(e, "Error while making a request to moonpay: ${response.request.url}")
        IOException("Error reading response error body", e)
    }
}
