package org.p2p.ethereumkit.internal.api.jsonrpcsubscription

import com.google.gson.Gson
import org.p2p.ethereumkit.internal.api.core.RpcSubscriptionResponse

abstract class RpcSubscription<T>(val params: List<Any>) {
    protected abstract val typeOfResult: Class<T>

    fun parse(response: RpcSubscriptionResponse, gson: Gson): T {
        return gson.fromJson(response.params.result.toString(), typeOfResult)
    }
}
