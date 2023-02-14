package org.p2p.wallet.infrastructure.network.interceptor

import android.net.Uri
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber
import java.io.IOException
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerError
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.rpc.RpcConstants

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

        val json = getRequestJson(request)
        val url = request.url
        val networkEnvironmentForRequest = getValidatedNetworkEnvironment(json)
        return request.newBuilder()
            .url(createRpcUrl(url, networkEnvironmentForRequest))
            .build()
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

    private fun getValidatedNetworkEnvironment(requestJson: JSONObject?): NetworkEnvironment {
        val methodKey = RpcConstants.REQUEST_METHOD_KEY
        val historyEndpoint = RpcConstants.REQUEST_METHOD_VALUE_GET_CONFIRMED_TRANSACTIONS
        val signaturesEndpoint = RpcConstants.REQUEST_METHOD_VALUE_GET_CONFIRMED_SIGNATURES
        return if (
            requestJson?.getString(methodKey) == historyEndpoint ||
            requestJson?.getString(methodKey) == signaturesEndpoint
        ) {
            NetworkEnvironment.RPC_POOL
        } else {
            currentEnvironment
        }
    }

    private fun createRpcUrl(originalUrl: HttpUrl, networkEnvironment: NetworkEnvironment): HttpUrl {
        val uriFromEnvironment = Uri.parse(networkEnvironment.endpoint)
        val newHost = uriFromEnvironment.host ?: error("Host cannot be null $uriFromEnvironment")

        return originalUrl.newBuilder()
            .host(newHost)
            .apply {
                if (networkEnvironment == NetworkEnvironment.RPC_POOL) {
                    addEncodedPathSegment(BuildConfig.rpcPoolApiKey)
                }
            }
            .build()
    }

    private fun handleResponse(response: Response): Response {
        val responseBody = try {
            response.body!!.string()
        } catch (e: Exception) {
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
        response.newBuilder().body(responseBody.toResponseBody()).build()

    private fun extractException(bodyString: String): Throwable = try {
        val fullMessage = JSONObject(bodyString).toString(1)

        Timber.tag("RpcInterceptor").d("Handling exception: $fullMessage")

        val serverError = gson.fromJson(bodyString, ServerError::class.java)

        val errorMessage = serverError.error.data?.getErrorLog() ?: serverError.error.message
        ServerException(
            errorCode = serverError.error.code ?: ErrorCode.SERVER_ERROR,
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
