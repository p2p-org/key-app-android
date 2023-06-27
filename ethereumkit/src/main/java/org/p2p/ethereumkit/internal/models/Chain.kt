package org.p2p.ethereumkit.internal.models

enum class Chain(
        val id: Int,
        val coinType: Int,
        val syncInterval: Long,
        val isEIP1559Supported: Boolean
) {
    Ethereum(1, 60, 15, true);

    val isMainNet = coinType != 1
}
