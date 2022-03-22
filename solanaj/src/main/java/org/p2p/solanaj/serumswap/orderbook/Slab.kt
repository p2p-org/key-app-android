package org.p2p.solanaj.serumswap.orderbook

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.PublicKey.Companion.PUBLIC_KEY_LENGTH
import org.p2p.solanaj.serumswap.model.Integer128
import org.p2p.solanaj.utils.ByteUtils.UINT_128_LENGTH
import org.p2p.solanaj.utils.ByteUtils.UINT_32_LENGTH
import org.p2p.solanaj.utils.ByteUtils.UINT_64_LENGTH
import java.math.BigInteger
import java.util.LinkedList

data class Slab(
    val headerLayout: HeaderLayout,
    val nodes: List<NodeLayout>
) {

    fun getNodeList(descending: Boolean = false): LinkedList<LeafNodeLayout> {
        val list = LinkedList<LeafNodeLayout>()

        if (headerLayout.leafCount == 0L) return list

        val stack = mutableListOf(headerLayout.root)
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

class HeaderLayout(
    val bumpIndex: Long,
    val zeros: Long,
    val freeListLen: Long,
    val zeros2: Long,
    val freeListHead: Long,
    val root: Long,
    val leafCount: Long,
    val zeros3: Long
) {

    companion object {
        const val HEADER_LENGTH = UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH +
            UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH
    }
}

class NodeLayout constructor(layoutType: SlabNodeLayoutType) {

    val value: SlabNodeLayoutType = layoutType

    companion object {
        const val NODE_LAYOUT_LENGTH = 4 + 68 // tag + node
    }
}

object UninitializedNodeLayout : SlabNodeLayoutType

class InnerNodeLayout(
    val prefixLen: Long,
    val key: BigInteger,
    val children: List<Long>
) : SlabNodeLayoutType {

    companion object {
        const val INNER_NODE_LENGTH =
            UINT_32_LENGTH + UINT_128_LENGTH + UINT_32_LENGTH + UINT_32_LENGTH
    }
}

class LeafNodeLayout constructor(
    val ownerSlot: Byte,
    val feeTier: Byte,
    val key: Integer128,
    val owner: PublicKey,
    val quantity: BigInteger,
    val clientOrderId: BigInteger,
) : SlabNodeLayoutType {

    companion object {
        const val LEAF_NODE_LENGTH = 1 + 1 + 2 + UINT_64_LENGTH + UINT_64_LENGTH +
            PUBLIC_KEY_LENGTH + UINT_64_LENGTH + UINT_64_LENGTH
    }
}

class FreeNodeLayout(val next: Long) : SlabNodeLayoutType

class LastFreeNodeLayout : SlabNodeLayoutType

interface SlabNodeLayoutType
