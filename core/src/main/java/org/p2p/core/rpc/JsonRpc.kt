package org.p2p.core.rpc

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.lang.reflect.Type

abstract class JsonRpc<P, T>(
    @SerializedName("method")
    val method: String,
    @SerializedName("params")
    val params: P
) {
    @SerializedName("jsonrpc")
    val version: String = "2.0"

    @SerializedName("id")
    var id: Int = 1

    protected abstract val typeOfResult: Type

    fun parseResponse(response: RpcResponse, gson: Gson): T {
        if (response.error != null) {
            val error = ResponseError.RpcError(response.error)
            Timber.e(error, "RPC error returned")
            throw error
        }
        return parseResult(response.result, gson)
    }

    fun parseResult(result: JsonElement?, gson: Gson): T {
        return try {
            gson.fromJson(result, typeOfResult) as T
        } catch (error: Throwable) {
            throw ResponseError.InvalidResult("Error: $error.toString()\nOn result: ${result.toString()}")
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
