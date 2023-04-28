package org.p2p.core.utils

object Constants {

    const val MIN_REQUIRED_ACCOUNT_INFO_DATA_LENGTH = 0

    const val SYMBOL_ZERO = "0"
    const val USD_SYMBOL = "$"
    const val USD_READABLE_SYMBOL = "USD"
    const val GBP_SYMBOL = "GBP"
    const val EUR_SYMBOL = "EUR"

    const val REN_BTC_SYMBOL = "renBTC"
    const val SOL_SYMBOL = "SOL"
    const val WSOL_SYMBOL = "wSOL"
    const val USDC_SYMBOL = "USDC"
    const val USDT_SYMBOL = "USDT"
    const val BTC_SYMBOL = "BTC"
    const val ETH_SYMBOL = "ETH"
    const val WETH_SYMBOL = "WETH"
    const val WBTC_SYMBOL = "WBTC"

    const val SOL_COINGECKO_ID = "solana"
    const val USDC_COINGECKO_ID = "usd-coin"
    const val BTC_COINGECKO_ID = "bitcoin"
    const val ETH_COINGECKO_ID = "ethereum"
    const val WETH_COINGECKO_ID = "ethereum-wormhole"
    const val USDT_COINGECKO_ID = "tether"

    const val WRAPPED_SOL_MINT = "So11111111111111111111111111111111111111112"
    const val WRAPPED_ETH_MINT = "7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"
    const val WRAPPED_BTC_MINT = "3NZ9JMVBmGAqocybic2c7LQCJScmgsAZ6vQqTDzcqmJh"

    const val FEE_RELAYER_ACCOUNT = "FG4Y3yX4AAchp1HvNZ7LfzFTewF2f6nDoMDCohTFrdpT"
    const val SWAP_FEE_RELAYER_ACCOUNT = "JdYkwaUrvoeYsCbPgnt3AAa1qzjV2CtoRqU3bzuAvQu"
    val FEE_RELAYER_ACCOUNTS = listOf(
        FEE_RELAYER_ACCOUNT,
        SWAP_FEE_RELAYER_ACCOUNT
    )
    const val REN_BTC_DEVNET_MINT = "FsaLodPu4VmSwXGr3gWfwANe4vKf8XSZcCh1CEeJ3jpD"
    const val REN_BTC_DEVNET_MINT_ALTERNATE = "CDJWUqTcYTVAKXAVXoQZFes5JUFc7owSeq7eMQcDSbo5"

    // Arbitrary mint to represent SOL (not wrapped SOL).
    const val SOL_MINT = "Ejmc1UB4EsES5oAaRN63SpoxMJidt3ZGBrqrZk49vjTZ"
    const val USDC_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
    const val USDT_MINT = "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"

    const val SOL_NAME = "Solana"
    const val ETH_NAME = "Ethereum"
    const val BTC_NAME = "Bitcoin"

    const val DEVICE_TYPE = "Android"

    const val ZERO_AMOUNT = "0"

    const val APP_HASH = "J1y9NEXfoHx"

    const val FIAT_FRACTION_LENGTH = 2
}
