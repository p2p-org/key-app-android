package org.p2p.ethereumkit.internal.models

enum class Chain(
    val id: Int,
    val coinType: Int,
    val syncInterval: Long,
    val isEIP1559Supported: Boolean
) {
    Ethereum(id = 1, coinType = 60, syncInterval = 15, isEIP1559Supported = true);

    val isMainNet = coinType != 1
}
