package org.p2p.solanaj.rpc

enum class Environment(val endpoint: String) {
    MAINNET("https://api.mainnet-beta.solana.com/"),
    SOLANA("https://solana-api.projectserum.com/"),
    RPC_POOL("https://p2p.rpcpool.com/"),
    DEVNET("https://api.devnet.solana.com/");
}
