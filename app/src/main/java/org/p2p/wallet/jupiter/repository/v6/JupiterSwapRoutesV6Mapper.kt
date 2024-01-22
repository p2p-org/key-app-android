package org.p2p.wallet.jupiter.repository.v6

import com.google.gson.Gson
import java.math.BigInteger
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.toJsonObject
import org.p2p.wallet.jupiter.api.response.SwapJupiterV6QuoteResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6

class JupiterSwapRoutesV6Mapper(
    private val gson: Gson
) {
    fun fromNetwork(
        response: SwapJupiterV6QuoteResponse,
        accountCreationFee: BigInteger,
    ): JupiterSwapRouteV6 {
        val plans = response.routePlan.map {
            val info = it.swapInfo
            JupiterSwapRoutePlanV6(
                ammKey = info.ammKey,
                feeAmount = info.feeAmount.toBigInteger(),
                feeMint = info.feeMint.toBase58Instance(),
                label = info.label,
                inputMint = info.inputMint.toBase58Instance(),
                outAmount = info.outAmount.toBigInteger(),
                outputMint = info.outputMint.toBase58Instance(),
                percent = it.percent.toString()
            )
        }

        return JupiterSwapRouteV6(
            inAmountInLamports = response.inAmount.toBigInteger(),
            outAmountInLamports = response.outAmount.toBigInteger(),
            priceImpactPercent = response.priceImpactPct.toBigDecimal(),
            slippageBps = response.slippageBps,
            otherAmountThreshold = response.otherAmountThreshold,
            swapMode = response.swapMode,
            ataFee = accountCreationFee,
            originalRoute = gson.toJsonObject(response),
            routePlans = plans
        )
    }
}
