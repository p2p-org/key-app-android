package org.p2p.ethereumkit.api.jsonrpcsubscription

data class RpcBlockHeader(val number: Long, val logsBloom: String)

class NewHeadsRpcSubscription : RpcSubscription<RpcBlockHeader>(listOf("newHeads")) {
    @Transient
    override val typeOfResult = RpcBlockHeader::class.java
}
