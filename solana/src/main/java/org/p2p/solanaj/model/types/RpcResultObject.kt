package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

open class RpcResultObject {
    class ContextData {
        @SerializedName("slot")
        val slot: Long = 0
    }

    @SerializedName("context")
    protected var context: ContextData? = null

    fun gContext(): ContextData? {
        return context
    }
}
