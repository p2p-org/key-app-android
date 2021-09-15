package org.p2p.solanaj.rpc

enum class Environment(val endpoint: String) {
    MAINNET("https://api.mainnet-beta.solana.com/"),
    TESTNET("https://api.testnet.solana.com/"),
    SOLANA("https://solana-api.projectserum.com/");
}