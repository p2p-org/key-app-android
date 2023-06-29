package org.p2p.wallet.jupiter.ui.main

import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.core.crypto.Base58String
import java.math.BigDecimal
import org.p2p.wallet.swap.model.Slippage

data class TestSwapRouteData(
    val swapPair: JupiterSwapPair,
    val userPublicKey: Base58String,
    val ratio: BigDecimal = JupiterSwapTestHelpers.SOL_TO_USD_RATE,
    val priceImpact: BigDecimal = BigDecimal("0.0000001"),
    val inDecimals: Int = 9,
    val outDecimals: Int = 9,
    var slippage: Slippage = Slippage.Medium,

    val inputMint: Base58String = swapPair.inputMint,
    val outputMint: Base58String = swapPair.outputMint,
    val amountIn: BigDecimal = swapPair.amountInLamports.toBigDecimal(inDecimals),
    val amountOut: BigDecimal = amountIn * ratio
) {
    val slippageBps: Int = (slippage.doubleValue * 10_000).toInt()
}
