// package org.p2p.wallet.swap.orca
//
// import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
// import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
// import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
// import org.p2p.wallet.swap.orca.utils.OrcaDataInitializer
// import junit.framework.Assert.assertEquals
// import kotlinx.coroutines.runBlocking
// import org.junit.After
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.robolectric.RobolectricTestRunner
// import java.math.BigInteger
//
// @RunWith(RobolectricTestRunner::class)
// class OrcaSwapPreparationTests {
//
//    val btcMint = "9n4nbM75f5Ui33ZbPYXn59EwSgE8CGsHtAeTH5YFeJ9E"
//    val ethMint = "2FPyTwcZLUg1MDrwsyoP4D6s1tM7hAkHYRjkNb5w6Pxk"
//    val socnMint = "5oVNBeEEQvYi1cX3ir8Dx5n1P7pdxydbGF2X4TxVusJm"
//
//    private val initializer = OrcaDataInitializer()
//
//    private lateinit var swapInteractor: OrcaSwapInteractor
//
//    @Before
//    fun setUp() {
//        initializer.initialize()
//        swapInteractor = initializer.getSwapInteractor()
//    }
//
//    @After
//    fun closeDb() {
//        initializer.closeDb()
//    }
//
//    @Test
//    fun testLoadSwap() = runBlocking {
// //        assertEquals(1035, swapInteractor.getSwapInfo()?.routes?.size)
//        assertEquals(6738, swapInteractor.getSwapInfo()?.routes?.size)
//        assertEquals(117, swapInteractor.getSwapInfo()?.tokens?.size)
//        assertEquals(71, swapInteractor.getSwapInfo()?.pools?.size)
//        assertEquals(
//            "SwaPpA9LAaLfeLi3a68M4DjnLqgtticKg6CnyNwgAC8",
//            swapInteractor.getSwapInfo()?.programIds?.serumTokenSwap
//        )
//        assertEquals(
//            "9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP",
//            swapInteractor.getSwapInfo()?.programIds?.tokenSwapV2
//        )
//        assertEquals(
//            "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1",
//            swapInteractor.getSwapInfo()?.programIds?.tokenSwap
//        )
//        assertEquals("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA", swapInteractor.getSwapInfo()?.programIds?.token)
//        assertEquals("82yxjeMsvaURa4MbZZ7WZZHfobirZYkH1zF8fmeGtyaQ", swapInteractor.getSwapInfo()?.programIds?.aquafarm)
//        assertEquals(117, swapInteractor.getSwapInfo()?.tokenNames?.size)
//    }
//
//    // Find destinations
//    @Test
//    fun testFindDestinations() = runBlocking {
//        val routes = swapInteractor.findPossibleDestinations(btcMint)
//        assertEquals(45, routes.size)
//    }
//
//    // MARK: - BTC -> ETH
//    // Order may change
//    //        [
//    //            [
//    //                "BTC/ETH"
//    //            ],
//    //            [
//    //                "BTC/SOL[aquafarm]",
//    //                "ETH/SOL"
//    //            ],
//    //            [
//    //                "BTC/SOL[aquafarm]",
//    //                "ETH/SOL[aquafarm]"
//    //            ]
//    //        ]
//    @Test
//    fun testGetTradablePoolsPairs() = runBlocking {
//        val pools = swapInteractor.getTradablePoolsPairs(btcMint, ethMint)
//        assertEquals(pools.size, 3) //
//        assertEquals(pools.flatten().size, 5)
//
//        val btcETHPool = pools.firstOrNull { it.size == 1 }?.first()!!
//        assertEquals(btcETHPool.tokenAccountA.toBase58(), "81w3VGbnszMKpUwh9EzAF9LpRzkKxc5XYCW64fuYk1jH")
//        assertEquals(btcETHPool.tokenAccountB.toBase58(), "6r14WvGMaR1xGMnaU8JKeuDK38RvUNxJfoXtycUKtC7Z")
//        assertEquals(btcETHPool.tokenAName, "BTC")
//        assertEquals(btcETHPool.tokenBName, "ETH")
//
//        val btcSOLAquafarm = pools.find { pair ->
//            pair.any { it.account.toBase58() == "7N2AEJ98qBs4PwEwZ6k5pj8uZBKMkZrKZeiC7A64B47u" }
//        }?.first()!!
//
//        assertEquals(btcSOLAquafarm.tokenAccountA.toBase58(), "9G5TBPbEUg2iaFxJ29uVAT8ZzxY77esRshyHiLYZKRh8")
//        assertEquals(btcSOLAquafarm.tokenAccountB.toBase58(), "5eqcnUasgU2NRrEAeWxvFVRTTYWJWfAJhsdffvc6nJc2")
//        assertEquals(btcSOLAquafarm.tokenAName, "BTC")
//        assertEquals(btcSOLAquafarm.tokenBName, "SOL")
//
//        val ethSOL = pools.find { pair ->
//            pair.any { it.account.toBase58() == "4vWJYxLx9F7WPQeeYzg9cxhDeaPjwruZXCffaSknWFxy" }
//        }?.last()!! // Reversed to SOL/ETH
//
//        assertEquals(
//            ethSOL.tokenAccountA.toBase58(),
//            "5x1amFuGMfUVzy49Y4Pc3HyCVD2usjLaofnzB3d8h7rv"
//        ) // originalTokenAccountB
//        assertEquals(
//            ethSOL.tokenAccountB.toBase58(),
//            "FidGus13X2HPzd3cuBEFSq32UcBQkF68niwvP6bM4fs2"
//        ) // originalTokenAccountA
//        assertEquals(ethSOL.tokenAName, "SOL")
//        assertEquals(ethSOL.tokenBName, "ETH")
//
//        val ethSOLAquafarm = pools.find { pair ->
//            pair.any { it.account.toBase58() == "EuK3xDa4rWuHeMQCBsHf1ETZNiEQb5C476oE9u9kp8Ji" }
//        }?.last()!! // Reversed to SOL/ETH
//        assertEquals(
//            ethSOLAquafarm.tokenAccountA.toBase58(),
//            "5pUTGvN2AA2BEzBDU4CNDh3LHER15WS6J8oJf5XeZFD8"
//        ) // originalTokenAccountB
//        assertEquals(
//            ethSOLAquafarm.tokenAccountB.toBase58(),
//            "7F2cLdio3i6CCJaypj9VfNDPW2DwT3vkDmZJDEfmxu6A"
//        ) // originalTokenAccountA
//        assertEquals(ethSOLAquafarm.tokenAName, "SOL")
//        assertEquals(ethSOLAquafarm.tokenBName, "ETH")
//    }
//
//    @Test
//    fun testGetBestPoolsPair() = runBlocking {
//        // when user enter input amount = 0.1 BTC
//        val inputAmount: BigInteger = BigInteger.valueOf(100000L) // 0.1 BTC
//        val poolsPairs = swapInteractor.getTradablePoolsPairs(btcMint, ethMint)
//        val bestPoolsPair = swapInteractor.findBestPoolsPairForInputAmount(inputAmount, poolsPairs)
//        val estimatedAmount = bestPoolsPair?.getOutputAmount(inputAmount)
//        assertEquals(BigInteger.valueOf(1588996L), estimatedAmount) // 1.588996 ETH
//
//        // when user enter estimated amount that he wants to receive as 1.6 ETH
//        val estimatedAmount2 = BigInteger.valueOf(1600000L)
//        val bestPoolsPair2 = swapInteractor.findBestPoolsPairForEstimatedAmount(estimatedAmount2, poolsPairs)
//        val inputAmount2 = bestPoolsPair2?.getInputAmount(estimatedAmount2)
//        assertEquals(BigInteger.valueOf(100697), inputAmount2) // 0.100697 BTC
//    }
//
//    // MARK: - SOCN -> SOL -> BTC (Reversed)
//    // SOCN -> BTC
//    //        [
//    //            [
//    //                "BTC/SOL[aquafarm]",
//    //                "SOCN/SOL[stable][aquafarm]"
//    //            ]
//    //        ]
//    // Should be considered at
//    //        [
//    //            [
//    //                "SOCN/SOL[stable][aquafarm]",
//    //                "BTC/SOL[aquafarm]"
//    //            ]
//    //
//    //
//    //      @_root_ide_package_.org.junit.Test  ]
//    @Test
//    fun testGetTradablePoolsPairsReversed() = runBlocking {
//        val poolsPair = swapInteractor.getTradablePoolsPairs(socnMint, btcMint).first()
//        assertEquals(poolsPair.size, 2) // there is only 1 pair
//
//        val socnSOL = poolsPair.first()
//        assertEquals("C8DRXUqxXtUgvgBR7BPAmy6tnRJYgVjG27VU44wWDMNV", socnSOL.tokenAccountA.toBase58())
//        assertEquals("DzdxH5qJ68PiM1p5o6PbPLPpDj8m1ZshcaMFATcxDZix", socnSOL.tokenAccountB.toBase58())
//        assertEquals("SOCN", socnSOL.tokenAName)
//        assertEquals("SOL", socnSOL.tokenBName)
//
//        val solBTC = poolsPair.last()
//        assertEquals("5eqcnUasgU2NRrEAeWxvFVRTTYWJWfAJhsdffvc6nJc2", solBTC.tokenAccountA.toBase58())
//        assertEquals("9G5TBPbEUg2iaFxJ29uVAT8ZzxY77esRshyHiLYZKRh8", solBTC.tokenAccountB.toBase58())
//        assertEquals("SOL", solBTC.tokenAName)
//        assertEquals("BTC", solBTC.tokenBName)
//    }
//
//    @Test
//    fun testGetBestPoolsPairReversed() = runBlocking {
//        // when user enter input amount = 419.68 SOCN
//        val inputAmount = BigInteger.valueOf(419680000000L) // 419.68 SOCN
//        val poolsPairs = swapInteractor.getTradablePoolsPairs(socnMint, btcMint)
//        val bestPoolsPair = swapInteractor.findBestPoolsPairForInputAmount(inputAmount, poolsPairs)
//        val estimatedAmount = bestPoolsPair?.getOutputAmount(inputAmount)
//        assertEquals(BigInteger.valueOf(1013077L), estimatedAmount) // 1.013077 BTC
//
//        // when user enter estimated amount that he wants to receive as 1 BTC
//        val estimatedAmount2 = BigInteger.valueOf(1000000L) // 1 BTC
//        val bestPoolsPair2 = swapInteractor.findBestPoolsPairForEstimatedAmount(estimatedAmount2, poolsPairs)
//        val inputAmount2 = bestPoolsPair2?.getInputAmount(estimatedAmount2)
//        assertEquals(BigInteger.valueOf(413909257520L), inputAmount2) // 413.909257520 BTC
//    }
// }
