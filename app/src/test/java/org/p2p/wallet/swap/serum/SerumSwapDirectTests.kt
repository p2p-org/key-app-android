// package org.p2p.wallet.swap.serum
//
// import kotlinx.coroutines.runBlocking
// import org.junit.After
// import org.junit.Assert.assertNotNull
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.p2p.solanaj.core.PublicKey
// import org.p2p.wallet.main.model.Token
// import org.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
// import org.p2p.wallet.swap.serum.utils.CoroutineTest
// import org.p2p.wallet.swap.serum.utils.SerumDataInitializer
// import org.robolectric.RobolectricTestRunner
// import java.math.BigDecimal
//
// @RunWith(RobolectricTestRunner::class)
// internal class SerumSwapDirectTests : CoroutineTest() {
//
//    companion object {
//        const val DEFAULT_SLIPPAGE = 0.005
//    }
//
//    private val initializer = SerumDataInitializer()
//
//    private lateinit var serumSwapInteractor: SerumSwapInteractor
//
//    private lateinit var userTokens: List<Token.Active>
//
//    @Before
//    fun setUp() {
//        initializer.initialize()
//        userTokens = initializer.getTokens()
//        serumSwapInteractor = initializer.getSwapInteractor()
//    }
//
//    @After
//    fun closeDb() {
//        initializer.closeDb()
//    }
//
//    private fun usdtWallet(): Token.Active = userTokens.first { it.tokenSymbol == "USDT" }
//    private fun usdcWallet(): Token.Active = userTokens.first { it.tokenSymbol == "USDC" }
//    private fun srmWallet(): Token.Active = userTokens.first { it.tokenSymbol == "SRM" }
//    private fun fidaWallet(): Token.Active = userTokens.first { it.tokenSymbol == "FIDA" }
//    private fun solNativeWallet(): Token.Active = userTokens.first { it.tokenSymbol == "SOL" }
//
//    private val SRM: PublicKey = PublicKey("SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt")
//    private val SOL: PublicKey = PublicKey("So11111111111111111111111111111111111111112")
//    private val USDC: PublicKey = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
//
//    @Test
//    fun `test swap SRM to USDC`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = srmWallet(),
//            toWallet = usdcWallet(),
//            amount = BigDecimal(0.1),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap SRM to USDT`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = srmWallet(),
//            toWallet = usdtWallet(),
//            amount = BigDecimal(0.1),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap USDC to SRM`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdcWallet(),
//            toWallet = srmWallet(),
//            amount = BigDecimal(2),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap USDT to SRM`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdtWallet(),
//            toWallet = srmWallet(),
//            amount = BigDecimal(2),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap SOL to USDC`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = solNativeWallet(),
//            toWallet = usdcWallet(),
//            amount = BigDecimal(0.1),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap SOL to USDT`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = solNativeWallet(),
//            toWallet = usdtWallet(),
//            amount = BigDecimal(0.1),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap USDC to SOL`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdcWallet(),
//            toWallet = solNativeWallet(),
//            amount = BigDecimal(7),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test swap USDT to SOL`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdtWallet(),
//            toWallet = solNativeWallet(),
//            amount = BigDecimal(7),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    fun `test swap FIDA to SRM`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = fidaWallet(),
//            toWallet = srmWallet(),
//            amount = BigDecimal(5),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test (special case) swap USDT to USDC`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdtWallet(),
//            toWallet = usdcWallet(),
//            amount = BigDecimal(7),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test (special case) swap USDC to USDT`() = runBlocking {
//        val transactionId = serumSwapInteractor.swap(
//            fromWallet = usdcWallet(),
//            toWallet = usdtWallet(),
//            amount = BigDecimal(7),
//            slippage = DEFAULT_SLIPPAGE,
//            isSimulation = true
//        )
//
//        assertNotNull(transactionId)
//    }
//
//    @Test
//    fun `test get price from new market`() = runBlocking {
//        val price = serumSwapInteractor.loadFair(SOL, SRM)
//        print(price)
//    }
// }
