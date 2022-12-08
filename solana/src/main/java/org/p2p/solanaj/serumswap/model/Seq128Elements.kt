package org.p2p.solanaj.serumswap.model

class Seq128Elements<T>(
    val elements: List<T>,
    val length: Int
) {

    companion object {
        const val LENGTH = 128
    }
}
