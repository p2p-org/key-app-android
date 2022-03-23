// package org.p2p.wallet.swap.serum
//
// import kotlinx.coroutines.runBlocking
// import org.junit.Assert.assertEquals
// import org.junit.Assert.assertNotNull
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.p2p.wallet.main.model.Token
// import org.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
// import org.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
// import org.p2p.wallet.swap.serum.utils.SerumDataInitializer
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.BTCUSDCMarket
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.SOL
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.SRM
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.SRMUSDCMarket
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.USDC
// import org.p2p.wallet.swap.serum.utils.SerumSwapData.USDT
// import org.p2p.wallet.utils.scaleMedium
// import org.robolectric.RobolectricTestRunner
// import java.math.BigDecimal
// import java.math.BigInteger
//
// @RunWith(RobolectricTestRunner::class)
// class SerumSwapMarketTests {
//
//    private val initializer = SerumDataInitializer()
//
//    private lateinit var serumSwapInteractor: SerumSwapInteractor
//    private lateinit var swapMarketInteractor: SerumSwapMarketInteractor
//
//    private lateinit var userTokens: List<Token.Active>
//
//    private fun solNativeWallet(): Token.Active = userTokens.first { it.tokenSymbol == "SOL" }
//
//    @Before
//    fun setUp() {
//        initializer.initialize()
//        serumSwapInteractor = initializer.getSwapInteractor()
//        swapMarketInteractor = initializer.getSwapMarketInteractor()
//        userTokens = initializer.getTokens()
//    }
//
//    @Test
//    fun `test get market`() = runBlocking {
//        // Swaps SRM -> USDC on the Serum orderbook.
//        val marketAddresses = swapMarketInteractor.route(SRM, USDC)
//        val marketAddress = marketAddresses?.firstOrNull()
//        val market = marketAddress?.let { serumSwapInteractor.loadMarket(it) }
//        assertNotNull(market)
//    }
//
//    @Test
//    fun testGetPriceFromCachedMarket() = runBlocking {
//        val srmUSDCPair = serumSwapInteractor.loadOrderbook(SRMUSDCMarket)
//        assertNotNull(srmUSDCPair)
//
//        val btcUSDCPair = serumSwapInteractor.loadOrderbook(BTCUSDCMarket)
//        assertNotNull(btcUSDCPair)
//
//        val srmBbo = serumSwapInteractor.loadBbo(srmUSDCPair)
//        assertNotNull(srmBbo)
//
//        val btcBbo = serumSwapInteractor.loadBbo(btcUSDCPair)
//        assertNotNull(btcBbo)
//    }
//
//    @Test
//    fun testGetMinOrderSizeFromCachedMarket() = runBlocking {
//        val minOrderSize = serumSwapInteractor.loadMinOrderSize(SOL.toBase58(), USDC.toBase58())
//        assertEquals(BigDecimal(0.1).scaleMedium(), minOrderSize)
//    }
//
//    @Test
//    fun `test calculate exchange rate`() {
//        val fair = 0.11853959222380275
//        val expectedRate = BigInteger.valueOf(8375353L)
//        val realRate = serumSwapInteractor.calculateExchangeRate(
//            fair = fair,
//            slippage = 0.005,
//            toDecimal = 6
//        )
//        assertEquals(expectedRate, realRate)
//    }
//
//    @Test
//    fun `test calculate network fees`() = runBlocking {
//        val lps = BigInteger.valueOf(5000L)
//        val mre = BigInteger.valueOf(2039280L)
//
//        // from native sol to new usdt wallet
//        val newUSDTWallet = Token.Other(
//            tokenSymbol = "USDT",
//            decimals = 6,
//            mintAddress = USDT.toBase58(),
//            tokenName = "USDT",
//            logoUrl = null,
//            color = 0,
//            serumV3Usdc = null,
//            serumV3Usdt = null,
//            isWrapped = false
//        )
//
//        val networkFees = serumSwapInteractor.calculateNetworkFee(
//            fromWallet = solNativeWallet(),
//            toWallet = newUSDTWallet,
//            lamportsPerSignature = lps,
//            minRentExemption = mre
//        )
//
//        // from usdc to srm
//        val usdcWallet = userTokens.first { it.mintAddress == USDC.toBase58() }
//
//        val newSRMWallet = Token.Other(
//            tokenSymbol = "SRM",
//            decimals = 6,
//            mintAddress = SRM.toBase58(),
//            tokenName = "Serum",
//            logoUrl = null,
//            color = 0,
//            serumV3Usdc = null,
//            serumV3Usdt = null,
//            isWrapped = false
//        )
//
//        val networkFees2 = serumSwapInteractor.calculateNetworkFee(
//            fromWallet = usdcWallet,
//            toWallet = newSRMWallet,
//            lamportsPerSignature = lps,
//            minRentExemption = mre
//        )
//
//        assertEquals(BigInteger.valueOf(27451320L), networkFees)
//        assertEquals(BigInteger.valueOf(25407040L), networkFees2)
//    }
// }
