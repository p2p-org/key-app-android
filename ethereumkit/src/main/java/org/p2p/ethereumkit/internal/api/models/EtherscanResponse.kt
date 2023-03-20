package org.p2p.ethereumkit.internal.api.models

data class EtherscanResponse(
        val status: String,
        val message: String,
        val result: List<Map<String, String>>
)
