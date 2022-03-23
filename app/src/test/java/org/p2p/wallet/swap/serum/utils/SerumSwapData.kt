package org.p2p.wallet.swap.serum.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.MarketStatLayout
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.crypto.Hex
import java.math.BigInteger

object SerumSwapData {

    val SRM = PublicKey("SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt")
    val USDT = PublicKey("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB")
    val USDC: PublicKey = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
    val SOL: PublicKey = PublicKey("So11111111111111111111111111111111111111112")
    val FIDA: PublicKey = PublicKey("EchesyfXePKdLtoiZSL8pBe8Myagyy8ZRqsACNCFGnvp")

    val BTCUSDCMarket = Market(
        decoded = MarketStatLayout.LayoutV2(
            accountFlags = AccountFlags(ByteUtils.readUint64(Hex.decode("0300000000000000"), 0)),
            ownAddress = PublicKey("A8YFbxQYFVqKZaoYJLLUVcQiWP7G2MeEgW5wsAQgMvFw"),
            vaultSignerNonce = BigInteger.valueOf(0),
            baseMint = PublicKey("9n4nbM75f5Ui33ZbPYXn59EwSgE8CGsHtAeTH5YFeJ9E"),
            quoteMint = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
            baseVault = PublicKey("GZ1YSupuUq9kB28kX9t1j9qCpN67AMMwn4Q72BzeSpfR"),
            baseDepositsTotal = BigInteger.valueOf(537318800L),
            baseFeesAccrued = BigInteger.valueOf(0L),
            quoteVault = PublicKey("7sP9fug8rqZFLbXoEj8DETF81KasaRA1fr6jQb6ScKc5"),
            quoteDepositsTotal = BigInteger.valueOf(12634544048534L),
            quoteFeesAccrued = BigInteger.valueOf(5387527075L),
            quoteDustThreshold = BigInteger.valueOf(100L),
            requestQueue = PublicKey("H6UaUrNVELJgTqao1CNL4252kShLKSfwoboT8tF7HNtB"),
            eventQueue = PublicKey("6NQqaa48SnBBJZt9HyVPngcZFW81JfDv9EjRX2M4WkbP"),
            bids = PublicKey("6wLt7CX1zZdFpa6uGJJpZfzWvG6W9rxXjquJDYiFwf9K"),
            asks = PublicKey("6EyVXMMA58Nf6MScqeLpw1jS12RCpry23u9VMfy8b65Y"),
            baseLotSize = BigInteger.valueOf(100L),
            quoteLotSize = BigInteger.valueOf(10L),
            feeRateBps = BigInteger.valueOf(0L),
            referrerRebatesAccrued = BigInteger.valueOf(1466925674L),
        ),
        baseSplTokenDecimals = 6,
        quoteSplTokenDecimals = 6,
        programId = SerumSwapProgram.dexPID
    )

    val SRMUSDCMarket = Market(
        programId = SerumSwapProgram.dexPID,
        decoded = MarketStatLayout.LayoutV2(
            accountFlags = AccountFlags(ByteUtils.readUint64(Hex.decode("0300000000000000"), 0)),
            ownAddress = PublicKey("ByRys5tuUWDgL73G8JBAEfkdFf8JWBzPBDHsBVQ5vbQA"),
            vaultSignerNonce = BigInteger.valueOf(0),
            baseMint = PublicKey("SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"),
            quoteMint = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
            baseVault = PublicKey("Ecfy8et9Mft9Dkavnuh4mzHMa2KWYUbBTA5oDZNoWu84"),
            baseDepositsTotal = BigInteger.valueOf(31441200000L),
            baseFeesAccrued = BigInteger.valueOf(0L),
            quoteVault = PublicKey("hUgoKy5wjeFbZrXDW4ecr42T4F5Z1Tos31g68s5EHbP"),
            quoteDepositsTotal = BigInteger.valueOf(605943927629L),
            quoteFeesAccrued = BigInteger.valueOf(2314109580L),
            quoteDustThreshold = BigInteger.valueOf(100L),
            requestQueue = PublicKey("Hr8Z93aWe4hhJbC5i7YTsPaSToziVh3vyMfv9GRqKFCh"),
            eventQueue = PublicKey("6o44a9xdzKKDNY7Ff2Qb129mktWbsCT4vKJcg2uk41uy"),
            bids = PublicKey("AuL9JzRJ55MdqzubK4EutJgAumtkuFcRVuPUvTX39pN8"),
            asks = PublicKey("8Lx9U9wdE3afdqih1mCAXy3unJDfzSaXFqAvoLMjhwoD"),
            baseLotSize = BigInteger.valueOf(100000L),
            quoteLotSize = BigInteger.valueOf(100L),
            feeRateBps = BigInteger.valueOf(0L),
            referrerRebatesAccrued = BigInteger.valueOf(1119325855L),
        ),
        baseSplTokenDecimals = 6,
        quoteSplTokenDecimals = 6
    )
}
