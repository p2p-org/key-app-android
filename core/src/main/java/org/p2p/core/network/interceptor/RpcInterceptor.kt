package org.p2p.core.network.interceptor

import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber
import java.io.IOException
import java.net.URI
import org.p2p.core.BuildConfig.rpcPoolApiKey
import org.p2p.core.network.data.EmptyDataException
import org.p2p.core.network.data.ErrorCode
import org.p2p.core.network.data.ServerErrorResponse
import org.p2p.core.network.data.ServerException
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.utils.toJsonObject

private const val TAG = "RpcInterceptor"

class RpcInterceptor(
    private val gson: Gson,
    private val environmentManager: NetworkEnvironmentManager
) : Interceptor {

    private val currentEnvironment: NetworkEnvironment
        get() = environmentManager.loadCurrentEnvironment()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = createRpcRequest(chain)
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            handleResponse(response)
        } else {
            val responseString = response.body!!.string()
            throw extractGeneralException(responseString)
        }
    }

    private fun createRpcRequest(chain: Interceptor.Chain): Request {
        val request = chain.request()
        val url = request.url
        return request.newBuilder()
            .url(createRpcUrl(url, currentEnvironment))
            .build()
    }

    private fun createRpcUrl(originalUrl: HttpUrl, networkEnvironment: NetworkEnvironment): HttpUrl {
        val uriFromEnvironment = URI.create(networkEnvironment.endpoint)
        val newHost = uriFromEnvironment.host ?: error("Host cannot be null $uriFromEnvironment")

        return originalUrl.newBuilder()
            .host(newHost)
            .apply {
                if (networkEnvironment == NetworkEnvironment.RPC_POOL) {
                    addEncodedPathSegment(rpcPoolApiKey)
                }
            }
            .build()
    }

    private fun handleResponse(response: Response): Response {
        val responseBody = try {
            response.body!!.string()
        } catch (e: Throwable) {
            throw IOException("Error parsing response body", e)
        }

        if (responseBody.isEmpty()) {
            throw EmptyDataException("Data is empty")
        }

        return try {
            when (val data = JSONTokener(responseBody).nextValue()) {
                is JSONObject -> parseObject(data, response, responseBody)
                is JSONArray -> parseArray(data, response, responseBody)
                else -> createResponse(response, responseBody)
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
                throw EmptyDataException("Empty data received from the server: $data")
            } else {
                createResponse(response, responseBody)
            }
        } else {
            throw extractException(responseBody)
        }
    }

    private fun createResponse(response: Response, responseBody: String): Response =
        response.newBuilder()
            .body(responseBody.toResponseBody())
            .build()

    private fun extractException(bodyString: String): Throwable = try {
        val fullMessage = JSONObject(bodyString).toString(1)

        Timber.tag("RpcInterceptor").d("Handling exception: $fullMessage")

        val serverError = gson.fromJson(bodyString, ServerErrorResponse::class.java)

        val errorMessage = serverError.error.data?.getErrorLog() ?: serverError.error.message

        val errorType: JsonElement? = serverError.error.data
            ?.rpcErrorDetails
            ?.takeIf { !it.isJsonNull }
        val domainErrorType: RpcTransactionError? = runCatching {
            gson.fromJson(errorType, RpcTransactionError::class.java)
        }.getOrNull()

        ServerException(
            errorCode = serverError.error.code ?: ErrorCode.SERVER_ERROR,
            fullMessage = fullMessage,
            errorMessage = errorMessage,
            jsonErrorBody = gson.toJsonObject(fullMessage),
            domainErrorType = domainErrorType
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
