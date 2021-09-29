package org.p2p.solanaj.rpc

enum class Environment(val endpoint: String) {
    MAINNET("https://api.mainnet-beta.solana.com/"),
    SOLANA("https://solana-api.projectserum.com/"),
    DEVNET("https://api.devnet.solana.com/");
}