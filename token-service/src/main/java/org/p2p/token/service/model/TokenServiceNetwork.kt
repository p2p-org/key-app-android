package org.p2p.token.service.model

enum class TokenServiceNetwork(val networkName: String) {
    SOLANA("solana"),
    ETHEREUM("ethereum");

    companion object {
        fun getValueOf(stringValue: String): TokenServiceNetwork {
            return TokenServiceNetwork.values().firstOrNull { it.networkName == stringValue }
                ?: SOLANA
        }
    }
}
