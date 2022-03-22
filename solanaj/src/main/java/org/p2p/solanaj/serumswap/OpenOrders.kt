package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey

class OpenOrders(
    val address: String,
    val data: OpenOrdersLayout,
    val programId: PublicKey
) {

    init {
        if (!data.accountFlags.initialized || !data.accountFlags.openOrders) {
            throw IllegalStateException("Invalid OpenOrders account")
        }
    }

    val version: Int?
        get() = Version.getVersion(programId.toBase58())

    val publicKey
        get() = PublicKey(address)

    companion object {
        private fun getLayoutType(programId: String): OpenOrdersLayout.Type {
            val version = Version.getVersion(programId)
            return if (version == 1) OpenOrdersLayout.Type.LAYOUT_V1
            else OpenOrdersLayout.Type.LAYOUT_V2
        }

        fun getLayoutSpan(programId: String): Long =
            getLayoutType(programId).span
    }
}
