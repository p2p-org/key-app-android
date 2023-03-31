package org.p2p.ethereumkit.external.model

import org.p2p.core.utils.Constants.ETH_COINGECKO_ID
import org.p2p.core.utils.Constants.ETH_NAME
import org.p2p.core.utils.Constants.ETH_SYMBOL
import org.p2p.core.utils.Constants.SOL_COINGECKO_ID
import org.p2p.core.utils.Constants.SOL_NAME
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_COINGECKO_ID
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_COINGECKO_ID
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_ETH_MINT
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.wrapper.eth.EthAddress

enum class ERC20Tokens(
    val contractAddress: String,
    val mintAddress: String,
    val coingeckoId: String,
    val tokenIconUrl: String? = null,
    val replaceTokenName: String? = null,
    val replaceTokenSymbol: String? = null,
    val receiveFromTokens: List<String> = listOf(mintAddress),
) {
    USDC(
        contractAddress = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
        mintAddress = "A9mUU4qviSctJVPJdBJWkb28deg915LYJKrzQ19ji3FM",
        coingeckoId = USDC_COINGECKO_ID,
        tokenIconUrl = "https://assets.coingecko.com/coins/images/6319/large/USD_Coin_icon.png?1547042389",
        replaceTokenName = "USD Coin",
        replaceTokenSymbol = USDC_SYMBOL,
        receiveFromTokens = listOf(
            "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
            "A9mUU4qviSctJVPJdBJWkb28deg915LYJKrzQ19ji3FM"
        )
    ),
    USDT(
        contractAddress = "0xdac17f958d2ee523a2206206994597c13d831ec7",
        mintAddress = "Dn4noZ5jgGfkntzcQSUZ8czkreiZ1ForXYoV2H8Dm7S1",
        coingeckoId = USDT_COINGECKO_ID,
        tokenIconUrl = "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
        replaceTokenName = "Tether USD",
        replaceTokenSymbol = USDT_SYMBOL,
        receiveFromTokens = listOf(
            "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB",
            "Dn4noZ5jgGfkntzcQSUZ8czkreiZ1ForXYoV2H8Dm7S1"
        )
    ),
    ETH(
        contractAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
        mintAddress = WRAPPED_ETH_MINT,
        coingeckoId = ETH_COINGECKO_ID,
        tokenIconUrl = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
        replaceTokenName = ETH_NAME,
        replaceTokenSymbol = ETH_SYMBOL,
        receiveFromTokens = listOf(
            "2FPyTwcZLUg1MDrwsyoP4D6s1tM7hAkHYRjkNb5w6Pxk",
            WRAPPED_ETH_MINT
        )
    ),
    SOL(
        contractAddress = "0xd31a59c85ae9d8edefec411d448f90841571b89c",
        mintAddress = WRAPPED_SOL_MINT,
        coingeckoId = SOL_COINGECKO_ID,
        tokenIconUrl = "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
        replaceTokenName = SOL_NAME,
        replaceTokenSymbol = SOL_SYMBOL
    ),
    AVAX(
        contractAddress = "0x85f138bfee4ef8e540890cfb48f620571d67eda3",
        mintAddress = "KgV1GvrHQmRBY8sHQQeUKwTm2r2h8t4C8qt12Cw1HVE",
        coingeckoId = "avalanche-2",
        tokenIconUrl = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
        replaceTokenName = "Avalanche",
    ),
    BNB(
        contractAddress = "0x418d75f65a02b3d53b2418fb8e1fe493759c7605",
        mintAddress = "9gP2kCy3wA1ctvYWQk75guqXuHfrEomqydHLtcTCqiLa",
        coingeckoId = "binancecoin",
        tokenIconUrl = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
        replaceTokenName = "BNB",
    ),
    MATIC(
        contractAddress = "0x7d1afa7b718fb893db30a3abc0cfc608aacfebb0",
        mintAddress = "C7NNPWuZCNjZBfW5p6JvGsR8pUdsRpEdP1ZAhnoDwj7h",
        coingeckoId = "matic-network",
        tokenIconUrl = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
        replaceTokenName = "Polygon",
        replaceTokenSymbol = "MATIC"
    ),
    CRV(
        contractAddress = "0xd533a949740bb3306d119cc777fa900ba034cd52",
        mintAddress = "7gjNiPun3AzEazTZoFEjZgcBMeuaXdpjHq2raZTmTrfs",
        coingeckoId = "curve-dao-token",
        tokenIconUrl = "https://assets.coingecko.com/coins/images/12124/large/Curve.png?1597369484",
        replaceTokenName = "Curve DAO Token"
    );

    companion object {
        fun findToken(contractAddress: EthAddress): ERC20Tokens {
            return values().first { contractAddress.hex.contains(it.contractAddress, ignoreCase = true) }
        }

        fun findTokenByMint(mintAddress: String): ERC20Tokens? {
            return values().firstOrNull() { mintAddress.contains(it.mintAddress, ignoreCase = true) }
        }
    }
}
