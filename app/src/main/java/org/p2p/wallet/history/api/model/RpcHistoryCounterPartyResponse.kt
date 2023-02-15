package org.p2p.wallet.history.api.model

data class RpcHistoryCounterPartyResponse(
     @SerializedName("address")
     val address: String,
     @SerializedName("username")
     val username: String? = null
 )
