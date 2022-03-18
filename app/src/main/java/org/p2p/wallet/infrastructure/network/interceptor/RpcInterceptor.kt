package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager

private const val BASE_URL_REGEX = "^.+?[^/:](?=[?/]|\$)/"

// todo: Update validation
class RpcInterceptor(
    private val gson: Gson,
    environmentManager: EnvironmentManager
) : ServerErrorInterceptor(gson) {

    private var currentEnvironment = environmentManager.loadEnvironment()

    init {
        environmentManager.setOnEnvironmentListener { newEnvironment ->
            currentEnvironment = newEnvironment
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = createRpcRequest(chain)
        val response = chain.proceed(request)
        return if (response.isSuccessful) {
            handleResponse(response)
        } else {
            throw extractGeneralException(response.body!!.string())
        }
    }

    private val requestBuffer = Buffer()
    private fun createRpcRequest(chain: Interceptor.Chain): Request {
        val request = chain.request()

        val json = getRequestJson(request)
        val newBuilder = request.newBuilder()
        var newUrl = extractNewUrl(request.url)

        // Refactor this
        if (json.getString("method") == "getConfirmedTransaction") {
            newUrl = extractNewUrl(request.url, Environment.RPC_POOL)
        }
        return newBuilder.url(newUrl).build()
    }

    private fun getRequestJson(request: Request): JSONObject {
        request.body?.writeTo(requestBuffer)
        val requestBodyString = requestBuffer.readUtf8()
        return if (requestBodyString.startsWith("[")) {
            val jsonArray = JSONArray(requestBodyString)
            jsonArray.getJSONObject(0)
        } else {
            JSONObject(requestBodyString)
        }
    }

    private fun extractNewUrl(url: HttpUrl, environment: Environment = currentEnvironment): String {
        val currentUrl = url.toString()
        val oldBaseUrl = BASE_URL_REGEX.toRegex().find(currentUrl)?.value.toString()
        return currentUrl.replace(oldBaseUrl, getBaseUrl(environment))
    }

    private fun getBaseUrl(environment: Environment): String = when (environment) {
        Environment.MAINNET -> environment.endpoint
        Environment.SOLANA -> environment.endpoint
        Environment.DEVNET -> environment.endpoint
        Environment.RPC_POOL -> {
            val rpcPoolApiKey = BuildConfig.rpcPoolApiKey
            if (rpcPoolApiKey.isNotBlank()) {
                "${Environment.RPC_POOL.endpoint}$rpcPoolApiKey/"
            } else {
                Environment.RPC_POOL.endpoint
            }
        }
    }
}