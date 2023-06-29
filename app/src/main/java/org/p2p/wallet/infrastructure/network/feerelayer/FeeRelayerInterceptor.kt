package org.p2p.wallet.infrastructure.network.feerelayer

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import org.p2p.core.network.data.ErrorCode
import java.io.IOException
import org.p2p.wallet.feerelayer.model.FeeRelayerException

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
            if (formattedBody.isEmpty()) {
                return FeeRelayerException(
                    errorCode = ErrorCode.SERVER_ERROR,
                    fullMessage = "No error body with code: $code",
                    errorMessage = null
                )
            }
            val fullMessage = JSONObject(formattedBody).toString(1)
            val serverError = gson.fromJson(formattedBody, FeeRelayerServerError::class.java)
            val error = FeeRelayerErrorMapper.fromFeeRelayer(
                serverError.code,
                serverError.data?.clientError?.firstOrNull().orEmpty()
            )
            return FeeRelayerException(
                errorCode = ErrorTypeConverter.fromFeeRelayer(error),
                fullMessage = fullMessage,
                errorMessage = error.message ?: serverError.message
            )
        } catch (e: Throwable) {
            throw IOException("Error reading response error body", e)
        }
    }
}
