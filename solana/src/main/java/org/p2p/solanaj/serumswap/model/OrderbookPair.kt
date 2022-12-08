package org.p2p.solanaj.serumswap.model

import org.p2p.solanaj.serumswap.orderbook.Orderbook

data class OrderbookPair(
    val bids: Orderbook,
    val asks: Orderbook
)
