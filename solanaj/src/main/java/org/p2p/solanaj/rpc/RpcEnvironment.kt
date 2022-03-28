package org.p2p.solanaj.rpc

enum class RpcEnvironment(
    val chain: String,
    val isTestnet: Boolean,
    val endpoint: String,
    val lightNode: String,
    val gatewayRegistry: String,
    val genesisHash: String,
    val p2shPrefix: Int
) {
    MAINNET(
        "mainnet", false, "https://ren.rpcpool.com/",
        "https://lightnode-mainnet.herokuapp.com", "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
        "5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d", 0x05
    ),

    DEVNET(
        "testnet", true, "https://api.devnet.solana.com/",
        "https://lightnode-testnet.herokuapp.com/", "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
        "EtWTRABZaYq6iMfeYKouRu166VU2xqa1wcaWoxPkrZBG", 0xc4
    )
}
