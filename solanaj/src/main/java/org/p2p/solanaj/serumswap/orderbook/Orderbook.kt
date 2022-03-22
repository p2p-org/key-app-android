package org.p2p.solanaj.serumswap.orderbook

import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.serumswap.model.Integer128
import org.p2p.solanaj.utils.toInt128
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

        if (!accountFlags.initialized || !(accountFlags.bids || accountFlags.asks)) {
            throw IllegalStateException("Invalid orderbook")
        }
    }

    fun getList(descending: Boolean = false): LinkedList<ListItem> {
        val list = LinkedList<ListItem>()

        slab.getNodeList(descending).forEach {
            val price = it.key.value.shr(64)
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

    class Layout(data: ByteArray) : AbstractData(data, LENGTH) {

        val accountFlags: AccountFlags
        val slab: Slab

        init {
            // just skipping 5 bytes
            readBytes(5)
            accountFlags = AccountFlags(readUint64())

            val headerLayout = parseHeaderLayout()
            val nodes = parseNodes(headerLayout)
            slab = Slab(headerLayout, nodes)

            // just skipping 7 bytes
            readBytes(7)
        }

        private fun parseHeaderLayout() = HeaderLayout(
            bumpIndex = readUint32(),
            zeros = readUint32(),
            freeListLen = readUint32(),
            zeros2 = readUint32(),
            freeListHead = readUint32(),
            root = readUint32(),
            leafCount = readUint32(),
            zeros3 = readUint32()
        )

        private fun parseNodes(headerLayout: HeaderLayout): List<NodeLayout> {
            val nodesList = mutableListOf<NodeLayout>()
            for (i in 0 until headerLayout.bumpIndex) {
                val node = handleNodeLayout()
                nodesList.add(node)
            }

            return nodesList
        }

        private fun handleNodeLayout(): NodeLayout {
            val startIndex = getCursorPosition()
            val type = when (val tag = readUint32()) {
                0L -> UninitializedNodeLayout
                1L -> parseInnerNode()
                2L -> parseLeafNode()
                3L -> FreeNodeLayout(readUint32())
                4L -> LastFreeNodeLayout()
                else -> throw IllegalStateException("Unsupported node $tag")
            }

            setCursorPosition(startIndex + NodeLayout.NODE_LAYOUT_LENGTH)
            return NodeLayout(type)
        }

        private fun parseInnerNode(): SlabNodeLayoutType {
            val prefixLen: Long = readUint32()
            val key: BigInteger = readUint128()
            val children: List<Long> = listOf(
                readUint32(),
                readUint32()
            )

            return InnerNodeLayout(prefixLen, key, children)
        }

        private fun parseLeafNode(): SlabNodeLayoutType {
            val ownerSlot: Byte = readByte()
            val feeTier: Byte = readByte()

            // just skipping two bytes
            readBytes(2)

            val key: Integer128 = readUint128().toInt128()
            val owner: PublicKey = readPublicKey()
            val quantity: BigInteger = readUint64()
            val clientOrderId: BigInteger = readUint64()
            return LeafNodeLayout(ownerSlot, feeTier, key, owner, quantity, clientOrderId)
        }

        companion object {
            const val LENGTH = AccountFlags.ACCOUNT_FLAGS_LENGTH +
                HeaderLayout.HEADER_LENGTH +
                NodeLayout.NODE_LAYOUT_LENGTH +
                NodeLayout.NODE_LAYOUT_LENGTH +
                NodeLayout.NODE_LAYOUT_LENGTH +
                NodeLayout.NODE_LAYOUT_LENGTH + 32
        }
    }

    enum class Side {
        BUY, SELL
    }
}

data class ListItem(
    val orderId: Integer128,
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
