package org.p2p.solanaj.model.serumswap.orderbook

import org.p2p.solanaj.model.serumswap.Market

data class Orderbook(
    val market: Market,
    val isBids: Boolean,
    val slab: Slab
)