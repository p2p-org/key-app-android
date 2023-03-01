package org.p2p.ethereumkit.api.jsonrpc

class SendRawTransactionJsonRpc(
        @Transient val signedTransaction: ByteArray
) : DataJsonRpc(
        method = "eth_sendRawTransaction",
        params = listOf(signedTransaction)
)
