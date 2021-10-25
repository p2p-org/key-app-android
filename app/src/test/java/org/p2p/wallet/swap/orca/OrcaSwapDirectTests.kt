package org.p2p.wallet.swap.orca

import org.p2p.wallet.main.model.Token
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor2
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.orca.utils.OrcaDataInitializer
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.socnSOLStableAquafarmsPool
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.socnUSDCAquafarmsPool
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.solNinjaAquafarmsPool
import org.p2p.wallet.swap.orca.utils.OrcaSwapData.usdcMNGOAquafarmsPool
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrcaSwapDirectTests {

    private val initializer = OrcaDataInitializer()

    private lateinit var swapInteractor: OrcaSwapInteractor2

    private lateinit var userTokens: List<Token.Active>

    val socnPubkey = "64DzCPdUpQUTnSgY6hP6ux125vY2v3aWbE4T4G42SM1j"
    val solPubkey = "3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG"
    val usdcPubkey = "3uetDDizgTtadDHZzyy9BqxrjQcozMEkxzbKhfZF4tG3"
    val slimPubkey = "ECHvg7FdfakbKQpeStwh1K3iU6XwfBQWMNrH7rUAQkN7"

    @Before
    fun setUp() {
        initializer.initialize()
        userTokens = initializer.getTokens()
        swapInteractor = initializer.getSwapInteractor()
    }

    @After
    fun closeDb() {
        initializer.closeDb()
    }

    // Direct SOL to SPL
    @Test
    fun testDirectSwapSOLToCreatedSPL() = runBlocking {
        val amount = 0.001 // 0.001 SOL to created SOCN

        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = solPubkey,
            toWalletPubkey = socnPubkey,
            bestPoolsPair = mutableListOf(socnSOLStableAquafarmsPool.reversed),
            amount = amount,
            slippage = 0.1,
            isSimulation = true
        )

        assert(swapSimulation is OrcaSwapResult.Success)
    }

    @Test
    fun testDirectSwapSOLToUncreatedSPL() = runBlocking {
        val amount = 0.001 // 0.001 SOL to uncreated

        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = solPubkey,
            toWalletPubkey = null,
            bestPoolsPair = mutableListOf(solNinjaAquafarmsPool),
            amount = amount,
            slippage = 0.1,
            isSimulation = true
        )

        assert(swapSimulation is OrcaSwapResult.Success)
    }

    // Direct SPL to SOL
    @Test
    fun testDirectSwapSPLToSOL() = runBlocking {
        val amount = 0.001 // 0.001 SOCN to Native SOL

        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = socnPubkey,
            toWalletPubkey = solPubkey,
            bestPoolsPair = mutableListOf(socnSOLStableAquafarmsPool),
            amount = amount,
            slippage = 0.1,
            isSimulation = true
        )

        assert(swapSimulation is OrcaSwapResult.Success)
    }

    // Direct SPL to SPL
    @Test
    fun testDirectSwapSPLToCreatedSPL() = runBlocking {
        val amount = 0.001 // 0.001 SOCN to USDC

        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = socnPubkey,
            toWalletPubkey = usdcPubkey,
            bestPoolsPair = mutableListOf(socnUSDCAquafarmsPool),
            amount = amount,
            slippage = 0.1,
            isSimulation = true
        )

        assert(swapSimulation is OrcaSwapResult.Success)
    }

    @Test
    fun testDirectSwapSPLToUncreatedSPL() = runBlocking {
        val amount = 0.1 // 0.1 USDC to MNGO

        val swapSimulation = swapInteractor.swap(
            fromWalletPubkey = usdcPubkey,
            toWalletPubkey = null,
            bestPoolsPair = mutableListOf(usdcMNGOAquafarmsPool),
            amount = amount,
            slippage = 0.1,
            isSimulation = true
        )

        assert(swapSimulation is OrcaSwapResult.Success)
    }
}