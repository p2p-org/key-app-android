package org.p2p.solanaj.serumswap.orderbook

import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigDecimal
import java.math.BigInteger
import java.util.LinkedList

data class Orderbook(
    val market: Market,
    val isBids: Boolean,
    val slab: Slab
) {

    constructor(market: Market, accountFlags: AccountFlags, slab: Slab) : this(
        market, accountFlags.bids, slab
    ) {

        if (!accountFlags.initialized || !(accountFlags.bids.xor(accountFlags.asks))) {
            throw IllegalStateException("Invalid orderbook")
        }
    }

    fun getList(descending: Boolean = false): LinkedList<ListItem> {
        val list = LinkedList<ListItem>()

        slab.getNodeList(descending).forEach {
            val price = getPriceFromKey(it.key)
            val item = ListItem(
                orderId = it.key,
                clientId = it.clientOrderId,
                openOrdersAddress = it.owner,
                openOrdersSlot = it.ownerSlot,
                feeTier = it.feeTier,
                price = market.priceLotsToNumber(price),
                priceLots = price,
                size = market.baseSizeLotsToNumber(it.quantity),
                sizeLots = it.quantity,
                side = if (isBids) Side.BUY else Side.SELL
            )

            list.add(item)
        }

        return list
    }

    private fun getPriceFromKey(key: PublicKey): BigInteger {
        // fixme: do parsing
        return BigInteger.TEN
    }

    class Layout(data: ByteArray) {

        val accountFlags: AccountFlags = AccountFlags(data)
        val slab: Slab = Slab(data)

        init {
            val dataLength = slab.dataLength + AccountFlags.ACCOUNT_FLAGS_LENGTH
            require(data.size >= dataLength) { "Wrong data" }
        }
    }

    enum class Side {
        BUY, SELL
    }
}

data class ListItem(
    val orderId: PublicKey,
    val clientId: BigInteger,
    val openOrdersAddress: PublicKey,
    val openOrdersSlot: Byte,
    val feeTier: Byte,
    val price: BigDecimal,
    val priceLots: BigInteger,
    val size: BigDecimal,
    val sizeLots: BigInteger,
    val side: Orderbook.Side
)