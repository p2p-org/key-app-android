package com.p2p.wallet.swapserum

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.serum.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapMarketInteractor
import com.p2p.wallet.swapserum.utils.CoroutineTest
import com.p2p.wallet.swapserum.utils.DataInitializer
import com.p2p.wallet.swapserum.utils.SerumSwapData.FIDA
import com.p2p.wallet.swapserum.utils.SerumSwapData.SRMUSDCMarket
import com.p2p.wallet.swapserum.utils.SerumSwapData.USDC
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SerumSwapTransitiveTests : CoroutineTest() {

    companion object {
        const val DEFAULT_SLIPPAGE = 0.005
    }

    private val initializer = DataInitializer()

    private lateinit var serumSwapInteractor: SerumSwapInteractor
    private lateinit var swapMarketInteractor: SerumSwapMarketInteractor

    private lateinit var userTokens: List<Token.Active>

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

    private fun srmWallet(): Token.Active = userTokens.first { it.tokenSymbol == "SRM" }
    private fun ethWallet(): Token.Active = userTokens.first { it.tokenSymbol == "ETH" }
    private fun btcWallet(): Token.Active = userTokens.first { it.tokenSymbol == "BTC" }
    private fun fidaWallet(): Token.Active = userTokens.first { it.tokenSymbol == "FIDA" }

    // Create from and to open orders and wait for confirmation before transitive swapping
    @Test
    fun `test create from and to open orders`() = runBlocking {
        val toMarket = serumSwapInteractor.loadMarkets(FIDA, USDC).first()

        val result = serumSwapInteractor.createFromAndToOpenOrdersForSwapTransitive(
            fromMarket = SRMUSDCMarket,
            toMarket = toMarket,
            feePayer = null,
            close = true,
            isSimulation = true
        )

        assertNotNull(result)
    }

    // / Swap SRM -> FIDA
    @Test
    fun `test swap SRM to FIDA`() = runBlocking {
        val tx = serumSwapInteractor.swap(
            fromWallet = srmWallet(),
            toWallet = fidaWallet(),
            amount = 1.toBigDecimal(),
            slippage = DEFAULT_SLIPPAGE,
            isSimulation = true
        )
        assertNotNull(tx)
    }

    // / Swap FIDA -> SRM
    @Test
    fun `test swap FIDA to SRM`() = runBlocking {
        val tx = serumSwapInteractor.swap(
            fromWallet = fidaWallet(),
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