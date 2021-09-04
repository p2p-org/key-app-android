package org.p2p.solanaj.serumswap.orderbook

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.PublicKey.Companion.PUBLIC_KEY_LENGTH
import org.p2p.solanaj.utils.ByteUtils.UINT_32_LENGTH
import org.p2p.solanaj.utils.ByteUtils.UINT_64_LENGTH
import java.math.BigInteger
import java.util.LinkedList

class Slab(data: ByteArray) {

    val header: HeaderLayout = HeaderLayout(data)
    val nodes: List<NodeLayout>

    val dataLength: Int

    init {

        val nodesList = mutableListOf<NodeLayout>()
        for (i in 0..header.bumpIndex) {
            nodesList.add(NodeLayout(data))
        }

        nodes = nodesList

        val nodesLength = nodes.size * NodeLayout.NODE_LAYOUT_LENGTH
        dataLength = HeaderLayout.HEADER_LENGTH + nodesLength

        require(data.size >= dataLength) { "Wrong data" }
    }

    fun getNodeList(descending: Boolean = false): LinkedList<LeafNodeLayout> {
        val list = LinkedList<LeafNodeLayout>()

        if (header.leafCount == 0L) return list

        val stack = mutableListOf(header.root)
        while (stack.size > 0) {
            val index = stack.removeLast()
            val node = nodes.getOrNull(index.toInt())?.value ?: continue

            when (node) {
                is LeafNodeLayout ->
                    list.add(node)
                is InnerNodeLayout -> {
                    if (descending) {
                        stack.add(node.children[0])
                        stack.add(node.children[1])
                    } else {
                        stack.add(node.children[1])
                        stack.add(node.children[0])
                    }
                }
            }
        }

        return list
    }
}

class HeaderLayout constructor(data: ByteArray) : AbstractData(data, HEADER_LENGTH) {

    companion object {
        const val HEADER_LENGTH = UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH +
            UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH
    }

    val bumpIndex: Long = readUint32()
    val zeros: Long = readUint32()
    val freeListLen: Long = readUint32()
    val zeros2: Long = readUint32()
    val freeListHead: Long = readUint32()
    val root: Long = readUint32()
    val leafCount: Long = readUint32()
    val zeros3: Long = readUint32()
}

class NodeLayout constructor(data: ByteArray) : AbstractData(data, NODE_LAYOUT_LENGTH) {

    val tag: Long = readUint32()
    val value: SlabNodeLayoutType = when (tag) {
        0L -> UninitializedNodeLayout
        1L -> InnerNodeLayout(data)
        2L -> LeafNodeLayout(data)
        3L -> FreeNodeLayout(data.size)
        4L -> LastFreeNodeLayout()
        else -> throw IllegalStateException("Unsupported node $tag")
    }

    companion object {
        const val NODE_LAYOUT_LENGTH = 4 + 68 // tag + node
    }
}

object UninitializedNodeLayout : SlabNodeLayoutType

class InnerNodeLayout constructor(data: ByteArray) : AbstractData(data, INNER_NODE_LENGTH), SlabNodeLayoutType {

    companion object {
        const val INNER_NODE_LENGTH =
            UINT_32_LENGTH + UINT_64_LENGTH + UINT_64_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH
    }

    val prefixLen: Long = readUint32()
    val key: PublicKey = readPublicKey()
    val children: List<Long> = listOf(
        readUint32(),
        readUint32()
    )
}

class LeafNodeLayout constructor(data: ByteArray) : AbstractData(data, LEAF_NODE_LENGTH), SlabNodeLayoutType {

    companion object {
        const val LEAF_NODE_LENGTH = 1 + 1 + 2 + UINT_64_LENGTH + UINT_64_LENGTH +
            PUBLIC_KEY_LENGTH + UINT_64_LENGTH + UINT_64_LENGTH
    }

    val ownerSlot: Byte = readByte()
    val feeTier: Byte = readByte()
    val emptyByte = readByte() // just skipping two bytes
    val emptyByte2 = readByte() // just skipping two bytes
    val key: PublicKey = readPublicKey()
    val owner: PublicKey = readPublicKey()
    val quantity: BigInteger = readUint64()
    val clientOrderId: BigInteger = readUint64()
}

class FreeNodeLayout(val next: Int) : SlabNodeLayoutType

class LastFreeNodeLayout : SlabNodeLayoutType

interface SlabNodeLayoutType