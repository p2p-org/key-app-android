package org.p2p.core.rpc

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.lang.reflect.Type

private const val TAG = "JsonRpcParser"

abstract class JsonRpc<Body, Response>(
    @SerializedName("method")
    val method: String,
    @SerializedName("params")
    val params: Body
) {
    @SerializedName("jsonrpc")
    val version: String = "2.0"

    @SerializedName("id")
    var id: Int = 1

    protected abstract val typeOfResult: Type

    fun parseResponse(response: RpcResponse, gson: Gson): Response {
        if (response.error != null) {
            val error = ResponseError.RpcError(response.error)
            Timber.tag(TAG).i(error, "RPC error returned")
            throw error
        }
        return parseResult(response.result, gson)
    }

    private fun parseResult(result: JsonElement?, gson: Gson): Response {
        return try {
            gson.fromJson(result, typeOfResult) as Response
        } catch (error: Throwable) {
            Timber.tag(TAG).i(result.toString())
            Timber.tag(TAG).i(error)
            throw ResponseError.InvalidResult("Error: $error.toString()\nOn result: $result")
        }
    }

    sealed class ResponseError(message: String) : Throwable(message = message) {
        data class RpcError(val error: RpcResponse.Error) : ResponseError(
            "Rpc returned error. message=${error.message};code=${error.code}"
        )

        data class InvalidResult(val result: Any?) : ResponseError(
            "Rpc returned invalid result type: ${result?.javaClass}"
        )
    }
}
