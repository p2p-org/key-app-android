package org.p2p.solanaj.rpc

enum class NetworkEnvironment(val endpoint: String) {
    MAINNET("https://api.mainnet-beta.solana.com"),
    SOLANA("https://solana-api.projectserum.com"),
    @Deprecated(message = "remove in 1.10.0", replaceWith = ReplaceWith("RPC_POOL"))
    OLD_RPC_POOL("https://p2p.rpcpool.com"),
    RPC_POOL("https://p2p-p2p-893b.mainnet.rpcpool.com"),
    DEVNET("https://api.devnet.solana.com");
}
