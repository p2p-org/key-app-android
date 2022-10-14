package org.p2p.wallet.solana.model

enum class NetworkStatusFrequency(val stringValue: String) {
    ONCE("Once"),
    MORE_THAN_ONCE("More_than_once");

    companion object {
        fun parse(value: String): NetworkStatusFrequency = when (value) {
            ONCE.stringValue -> ONCE
            MORE_THAN_ONCE.stringValue -> MORE_THAN_ONCE
            else -> ONCE
        }
    }
}
