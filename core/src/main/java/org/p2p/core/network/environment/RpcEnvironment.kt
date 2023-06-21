package org.p2p.core.network.environment

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
        chain = "mainnet",
        isTestnet = false,
        endpoint = "https://ren.rpcpool.com/",
        lightNode = "https://lightnode-mainnet.herokuapp.com",
        gatewayRegistry = "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
        genesisHash = "5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d",
        p2shPrefix = 0x05
    ),
    DEVNET(
        chain = "testnet",
        isTestnet = true,
        endpoint = "https://api.devnet.solana.com/",
        lightNode = "https://lightnode-testnet.herokuapp.com/",
        gatewayRegistry = "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
        genesisHash = "EtWTRABZaYq6iMfeYKouRu166VU2xqa1wcaWoxPkrZBG",
        p2shPrefix = 0xc4
    )
}
