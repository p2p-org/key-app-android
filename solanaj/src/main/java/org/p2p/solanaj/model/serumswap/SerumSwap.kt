package org.p2p.solanaj.model.serumswap

class SerumSwap {

    enum class Side {
        BID, ASK;

        fun getParams() =
            when (this) {
                BID -> "bid" to Pair("", "")
                ASK -> "ask" to Pair("", "")
            }

        fun getBytes() = when (this) {
            BID -> 0
            ASK -> 1
        }
    }
}