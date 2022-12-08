package org.p2p.solanaj.serumswap.model

// Side rust enum used for the program's RPC API.
enum class Side {
    BID, ASK;

    fun getParams() =
        when (this) {
            BID -> "bid" to Pair("", "")
            ASK -> "ask" to Pair("", "")
        }

    fun getBytes(): Int = when (this) {
        BID -> 0
        ASK -> 1
    }
}
