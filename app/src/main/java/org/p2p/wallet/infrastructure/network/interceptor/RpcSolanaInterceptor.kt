package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerError
import org.p2p.wallet.infrastructure.network.data.ServerException
import timber.log.Timber
import java.io.IOException

// todo: Update validation
private const val GSON_KEY = "method"
private const val GSON_VALUE = "getConfirmedTransaction"

open class RpcSolanaInterceptor(private val gson: Gson) : Interceptor {

    private val TAG = "RpcInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("Content-Type", "application/json").build()
        Timber.tag("RpcInterceptor").d("Request = ${getRequestJson(request)}")
        val response = chain.proceed(request)
        val responseString = response.body?.string().orEmpty()
        Timber.tag(TAG).d(responseString)
        return if (response.isSuccessful) {
            handleResponse(response, responseString)
        } else {
            throw extractGeneralException(responseString)
        }
    }

    private fun getRequestJson(request: Request): JSONObject? {
        val requestBuffer = Buffer()

        request.body?.writeTo(requestBuffer)

        val requestBodyString = requestBuffer.readUtf8()

        val json: JSONObject = try {
            when (val data = JSONTokener(requestBodyString).nextValue()) {
                is JSONObject -> data
                is JSONArray -> data.get(0) as JSONObject
                else -> throw IllegalStateException("Unknown type of request body")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error on parsing json $e")
            return null
        }

        return json
    }

    private fun handleResponse(response: Response, responseString: String): Response {
        if (responseString.isEmpty()) {
            throw EmptyDataException("Data is empty")
        }

        return try {
            when (val data = JSONTokener(responseString).nextValue()) {
                is JSONObject -> parseObject(data, response, responseString)
                is JSONArray -> parseArray(data, response, responseString)
                else -> createResponse(response, responseString)
            }
        } catch (e: JSONException) {
            throw IllegalStateException("Error parsing data", e)
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
                throw EmptyDataException("Empty data received from the server")
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
        val fullMessage = JSONObject(bodyString).toString(1)

        Timber.tag("ServerErrorInterceptor").d("Handling exception: $fullMessage")

        val serverError = gson.fromJson(bodyString, ServerError::class.java)

        val errorMessage = serverError.error.data?.getErrorLog() ?: serverError.error.message
        ServerException(
            errorCode = serverError.error.code,
            fullMessage = fullMessage,
            errorMessage = errorMessage
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
        IOException("Error reading response error body: $bodyString", e)
    }
}
