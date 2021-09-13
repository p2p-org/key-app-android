package com.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.p2p.wallet.infrastructure.network.EmptyDataException
import com.p2p.wallet.infrastructure.network.ErrorCode
import com.p2p.wallet.infrastructure.network.ServerError
import com.p2p.wallet.infrastructure.network.ServerException
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

// todo: Update validation
class ServerErrorInterceptor(
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            handleResponse(response)
        } else {
            throw extractGeneralException(response.body!!.string())
        }
    }

    private fun handleResponse(response: Response): Response {
        val responseBody = try {
            response.body!!.string()
        } catch (e: Exception) {
            throw IOException("Error reading response error body", e)
        }

        return when (val data = JSONTokener(responseBody).nextValue()) {
            is JSONObject -> parseObject(data, response, responseBody)
            is JSONArray -> parseArray(data, response, responseBody)
            else -> createResponse(response, responseBody)
        }
    }

    private fun parseArray(
        data: JSONArray,
        response: Response,
        responseBody: String
    ): Response {
        val firstItem = data.get(0) as JSONObject
        val result = firstItem.optJSONObject("result")
        return if (result != null) {
            createResponse(response, responseBody)
        } else {
            throw extractException(responseBody)
        }
    }

    private fun parseObject(
        data: JSONObject,
        response: Response,
        responseBody: String
    ): Response {
        val error = data.optString("error")
        return if (error.isNullOrEmpty()) {
            /*
            * We have a case when result is an empty array, we should show empty state in the UI
            * Making custom exception and not showing error on exactly this exception
            * Temporary hack
            *  */
            val result = data.optJSONArray("result")
            if (result?.length() == 0) {
                throw EmptyDataException("Unexpected data received from the server")
            } else {
                createResponse(response, responseBody)
            }
        } else {
            throw extractException(responseBody)
        }
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

    private fun extractGeneralException(bodyString: String): Throwable = try {
        ServerException(
            errorCode = ErrorCode.SERVER_ERROR,
            fullMessage = bodyString,
            errorMessage = null
        )
    } catch (e: Throwable) {
        IOException("Error reading response error body", e)
    }
}