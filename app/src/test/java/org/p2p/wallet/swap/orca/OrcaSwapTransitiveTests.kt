// package org.p2p.wallet.swap.orca
//
// import kotlinx.coroutines.runBlocking
// import org.junit.After
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
// import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
// import org.p2p.wallet.swap.model.orca.OrcaSwapResult
// import org.p2p.wallet.swap.orca.utils.OrcaDataInitializer
// import org.robolectric.RobolectricTestRunner
//
// @RunWith(RobolectricTestRunner::class)
// class OrcaSwapTransitiveTests {
//
//    private val initializer = OrcaDataInitializer()
//
//    private lateinit var swapInteractor: OrcaSwapInteractor
//    private lateinit var tokenKeyProvider: TokenKeyProvider
//
//    private val slimPubkey = "ECHvg7FdfakbKQpeStwh1K3iU6XwfBQWMNrH7rUAQkN7"
//
//    @Before
//    fun setUp() {
//        initializer.initialize(false)
//        tokenKeyProvider = initializer.getTokenKeyProvider()
//        swapInteractor = initializer.getSwapInteractor()
//    }
//
//    @After
//    fun closeDb() {
//        initializer.closeDb()
//    }
//
//    // Transitive SOL to SPL
// //    @Test
// //    fun testTransitiveSwapSOLToCreatedSPL() = runBlocking {
// //        val amount: Double = 0.01 // 0.001 SOL to created KURO
// //        val solPubkey = tokenKeyProvider.publicKey
// //
// //        val kuroPubkey = ""
// //        val swapSimulation = swapInteractor.swap(
// //            fromWalletPubkey = solPubkey,
// //            toWalletPubkey = kuroPubkey,
// //            bestPoolsPair = mutableListOf(solUSDCAquafarmsPool, usdcKUROAquafarmsPool),
// //            amount = amount,
// //            slippage = 0.05,
// //            isSimulation = true
// //        )
// //
// //        assertEquals(swapSimulation is OrcaSwapResult.Success, true)
// //    }
//
//    @Test
//    fun testTransitiveSwapSPLToUncreatedSPL() = runBlocking {
//        val swapSimulation = initializer.fillPoolsBalancesAndSwap(
//            fromWalletPubkey = slimPubkey,
//            toWalletPubkey = null,
//            bestPoolsPair = listOf(
//                OrcaDataInitializer.RawPool("SLIM/USDC[aquafarm]", false),
//                OrcaDataInitializer.RawPool("ABR/USDC[aquafarm]", true)
//            ),
//            amount = 0.01,
//            slippage = 0.05,
//            isSimulation = true
//        )
//
//        assert(swapSimulation is OrcaSwapResult.Success)
//    }
// }
