package org.p2p.ethereumkit.internal.api.jsonrpc

class GasPriceJsonRpc : LongJsonRpc(
        method = "eth_gasPrice",
        params = listOf()
)
