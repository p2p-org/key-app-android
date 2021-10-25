package org.p2p.wallet.swap.orca

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor2
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.orca.utils.OrcaDataInitializer
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.solUSDCAquafarmsPool
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.usdcKUROAquafarmsPool
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/* TODO: not ready yet */
// @RunWith(RobolectricTestRunner::class)
class OrcaSwapTransitiveTests {

    private val initializer = OrcaDataInitializer()

    private lateinit var swapInteractor: OrcaSwapInteractor2
    private lateinit var tokenKeyProvider: TokenKeyProvider

    @Before
    fun setUp() {
        initializer.initialize()
        tokenKeyProvider = initializer.getTokenKeyProvider()
        swapInteractor = initializer.getSwapInteractor()
    }

    @After
    fun closeDb() {
        initializer.closeDb()
    }

    // Transitive SOL to SPL
    fun testTransitiveSwapSOLToCreatedSPL() = runBlocking {
        val amount: Double = 0.01 // 0.001 SOL to created KURO
        val solPubkey = tokenKeyProvider.publicKey

        val kuroPubkey = ""
        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = solPubkey,
            toWalletPubkey = kuroPubkey,
            bestPoolsPair = mutableListOf(solUSDCAquafarmsPool, usdcKUROAquafarmsPool),
            amount = amount,
            slippage = 0.05,
            isSimulation = true
        )

        assertEquals(swapSimulation is OrcaSwapResult.Success, true)
    }
}