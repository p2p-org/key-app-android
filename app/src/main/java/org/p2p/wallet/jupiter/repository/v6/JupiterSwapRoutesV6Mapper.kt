package org.p2p.wallet.jupiter.repository.v6

import com.google.gson.Gson
import org.json.JSONObject
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toJsonObject
import org.p2p.wallet.jupiter.api.response.SwapJupiterV6QuoteResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.SwapKeyAppFees
import org.p2p.wallet.utils.mapAsStrings

class JupiterSwapRoutesV6Mapper(
    private val gson: Gson
) {
    fun fromNetwork(
        response: SwapJupiterV6QuoteResponse,
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
        val feesJson = JSONObject(response.keyAppFees.feeDetails.toString())
        val fees = SwapKeyAppFees(
            totalFees = response.keyAppFees.fee.toBigInteger(),
            signatureFee = feesJson.optLong("signatureFee").toBigInteger(),
            ataDeposits = feesJson.optJSONArray("ataDeposits")
                ?.mapAsStrings { it.toBigInteger() }
                ?.sumOf { it }
                .orZero(),
            totalFeeAndDeposits = feesJson.optLong("totalFeeAndDeposits").toBigInteger(),
            minimumSolForTransaction = feesJson.optLong("minimumSOLForTransaction").toBigInteger(),
        )

        return JupiterSwapRouteV6(
            inAmountInLamports = response.inAmount.toBigInteger(),
            outAmountInLamports = response.outAmount.toBigInteger(),
            priceImpactPercent = response.priceImpactPct.toBigDecimal(),
            slippageBps = response.slippageBps,
            otherAmountThreshold = response.otherAmountThreshold,
            swapMode = response.swapMode,
            originalRoute = gson.toJsonObject(response),
            routePlans = plans,
            fees = fees
        )
    }
}
