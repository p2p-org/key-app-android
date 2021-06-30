package com.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.p2p.wallet.infrastructure.network.ServerError
import com.p2p.wallet.infrastructure.network.ServerException
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

class ServerErrorInterceptor(
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return handleResponse(response)
    }

    private fun handleResponse(response: Response): Response {
        try {
            val bodyString = response.body!!.string()
            val bodyData = JSONObject(bodyString)
            val error = bodyData.optString("error")

            return if (error.isNullOrEmpty()) {
                response
                    .newBuilder()
                    .body(bodyString.toResponseBody())
                    .build()
            } else {
                throw extractException(bodyString)
            }
        } catch (e: ServerException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Error reading response error body", e)
        }
    }

    private fun extractException(bodyString: String): Throwable = try {
        val serverError = gson.fromJson(bodyString, ServerError::class.java)

        ServerException(
            errorCode = serverError.error.code,
            fullMessage = JSONObject(bodyString).toString(1),
            errorMessage = serverError.error.message
        )
    } catch (e: Throwable) {
        IOException("Error reading response error body", e)
    }
}