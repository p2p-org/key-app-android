package com.p2p.wallet.swap

import com.p2p.wallet.common.crypto.Hex
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapMarketInteractor
import com.p2p.wallet.swap.utils.CoroutineTest
import com.p2p.wallet.swap.utils.DataInitializer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.MarketStatLayout
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.dexPID
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.utils.ByteUtils
import org.robolectric.RobolectricTestRunner
import java.math.BigInteger

@RunWith(RobolectricTestRunner::class)
internal class SerumSwapTransitiveTests : CoroutineTest() {

    companion object {
        const val DEFAULT_SLIPPAGE = 0.005
    }

    private val initializer = DataInitializer()

    private lateinit var serumSwapInteractor: SerumSwapInteractor
    private lateinit var swapMarketInteractor: SwapMarketInteractor

    private lateinit var userTokens: List<Token>

    @Before
    fun setUp() {
        initializer.initialize()
        userTokens = initializer.getTokens()
        serumSwapInteractor = initializer.getSwapInteractor()
        swapMarketInteractor = initializer.getSwapMarketInteractor()
    }

    @After
    fun closeDb() {
        initializer.closeDb()
    }

    private fun srmWallet(): Token = userTokens.first { it.tokenSymbol == "SRM" }
    private fun ethWallet(): Token = userTokens.first { it.tokenSymbol == "ETH" }
    private fun btcWallet(): Token = userTokens.first { it.tokenSymbol == "BTC" }
    private fun oxyWallet(): Token = userTokens.first { it.tokenSymbol == "OXY" }

    private val USDC: PublicKey = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
    private val OXY: PublicKey = PublicKey("z3dn17yLaGMKffVogeFHQ9zWVcXgqgf3PQnDsNs2g6M")

    private val SRMUSDCMarket = Market(
        programId = dexPID,
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

    // Create from and to open orders and wait for confirmation before transitive swapping
    @Test
    fun `test create from and to open orders`() = runBlocking {
        val toMarket = serumSwapInteractor.loadMarkets(OXY, USDC).first()

        val result = serumSwapInteractor.createFromAndToOpenOrdersForSwapTransitive(
            fromMarket = SRMUSDCMarket,
            toMarket = toMarket,
            feePayer = null,
            close = true,
            isSimulation = true
        )

        assertNotNull(result)
    }

    // / Swap SRM -> OXY
    @Test
    fun `test swap SRM to OXY`() = runBlocking {
        val tx = serumSwapInteractor.swap(
            fromWallet = srmWallet(),
            toWallet = oxyWallet(),
            amount = 1.toBigDecimal(),
            slippage = DEFAULT_SLIPPAGE,
            isSimulation = true
        )
        assertNotNull(tx)
    }

    // / Swap OXY -> SRM
    @Test
    fun `test swap OXY to SRM`() = runBlocking {
        val tx = serumSwapInteractor.swap(
            fromWallet = oxyWallet(),
            toWallet = srmWallet(),
            amount = 5.toBigDecimal(),
            slippage = DEFAULT_SLIPPAGE,
            isSimulation = true
        )
        assertNotNull(tx)
    }

    // / Swaps ETH -> BTC on the Serum orderbook.
    @Test
    fun `test swap ETH to BTC`() = runBlocking {
        val tx = serumSwapInteractor.swap(
            fromWallet = ethWallet(),
            toWallet = btcWallet(),
            amount = 0.00005.toBigDecimal(),
            slippage = DEFAULT_SLIPPAGE,
            isSimulation = true
        )
        assertNotNull(tx)
    }
}