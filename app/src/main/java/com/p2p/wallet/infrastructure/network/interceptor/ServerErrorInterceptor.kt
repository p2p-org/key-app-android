package com.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.p2p.wallet.infrastructure.network.ServerError
import com.p2p.wallet.infrastructure.network.ServerException
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

class ServerErrorInterceptor(
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return handleResponse(response)
    }

    private fun handleResponse(response: Response): Response =
        try {
            val responseBody = response.body!!.string()
            when (val data = JSONTokener(responseBody).nextValue()) {
                is JSONObject -> {
                    val error = data.optString("error")
                    if (error.isNullOrEmpty()) {
                        createResponse(response, responseBody)
                    } else {
                        throw extractException(responseBody)
                    }
                }
                is JSONArray -> {
                    val firstItem = data.get(0) as JSONObject
                    val result = firstItem.optJSONObject("result")
                    if (result != null) {
                        createResponse(response, responseBody)
                    } else {
                        throw extractException(responseBody)
                    }
                }
                else -> {
                    createResponse(response, responseBody)
                }
            }
        } catch (e: Exception) {
            throw IOException("Error reading response error body", e)
        }

    private fun createResponse(response: Response, responseBody: String): Response =
        response.newBuilder().body(responseBody.toResponseBody()).build()

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