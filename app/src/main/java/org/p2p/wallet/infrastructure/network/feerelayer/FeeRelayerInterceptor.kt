package org.p2p.wallet.infrastructure.network.feerelayer

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import timber.log.Timber
import java.io.IOException

class FeeRelayerInterceptor(
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            response
        } else {
            throw extractException(response.body!!.string(), response.code)
        }
    }

    private fun extractException(bodyString: String, code: Int): Throwable {
        try {
            val formattedBody = bodyString.replace("\\\"Program [^\\\"]+\\\"", "")
            Timber.tag("FeeRelayerInterceptor").e("Error received. Code: $code, error body: $formattedBody")

            if (formattedBody.isEmpty()) {
                return ServerException(
                    errorCode = ErrorCode.SERVER_ERROR,
                    fullMessage = "No error body",
                    errorMessage = null
                )
            }
            val fullMessage = JSONObject(formattedBody).toString(1)
            val serverError = gson.fromJson(formattedBody, FeeRelayerServerError::class.java)
            return ServerException(
                errorCode = ErrorTypeConverter.fromFeeRelayer(serverError.data?.type ?: FeeRelayerErrorType.UNKNOWN),
                fullMessage = fullMessage,
                errorMessage = serverError.message
            )
        } catch (e: Throwable) {
            throw IOException("Error reading response error body", e)
        }
    }
}
