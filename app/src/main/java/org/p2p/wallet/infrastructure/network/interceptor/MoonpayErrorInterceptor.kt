package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.feerelayer.ErrorTypeConverter
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayError
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorType
import timber.log.Timber
import java.io.IOException

class MoonpayErrorInterceptor(private val gson: Gson) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            response
        } else {
            throw extractException(response.body!!.string())
        }
    }

    private fun extractException(responseBody: String): Throwable = try {
        Timber.tag("Moonpay").e("Extracting exception: $responseBody")
        val serverError = gson.fromJson(responseBody, MoonpayError::class.java)
        val type = MoonpayErrorType.parse(serverError.type)
        val errorCode = ErrorTypeConverter.fromMoonpay(type)
        ServerException(
            errorCode = errorCode,
            fullMessage = serverError.message,
            errorMessage = serverError.message
        )
    } catch (e: Throwable) {
        IOException("Error reading response error body", e)
    }
}
