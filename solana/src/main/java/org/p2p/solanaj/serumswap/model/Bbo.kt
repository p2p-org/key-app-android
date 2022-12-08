package org.p2p.solanaj.serumswap.model

class Bbo(
    val bestBids: Double?,
    val bestOffer: Double?
) {

    val mid: Double?
        get() {
            var d = 2
            if (bestBids == null) {
                d -= 1
            }
            if (bestOffer == null) {
                d -= 1
            }
            if (d == 0) return null
            return ((bestBids ?: 0.0) + (bestOffer ?: 0.0)) / d
        }
}
