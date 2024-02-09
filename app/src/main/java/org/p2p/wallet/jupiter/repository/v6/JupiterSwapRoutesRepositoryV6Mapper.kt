package org.p2p.wallet.jupiter.repository.v6

import com.google.gson.Gson
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toJsonObject
import org.p2p.wallet.jupiter.api.response.SwapJupiterV6QuoteResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.SwapKeyAppFees
import org.p2p.wallet.utils.mapAsStrings

class JupiterSwapRoutesRepositoryV6Mapper(
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

        // feeBps=20 == 2%
        // feeBps=200 == 20%
        // feeBps=100 == 10%
        // feeBps=2 == 0.2%
        val platformFeePercent = getPlatformFeePercent(response.platformFee)
        val platformFeeAmount = response.platformFee?.amount?.toBigInteger().orZero()

        val ataDeposits = getAtaDepositsSum(feesJson)
        val fees = SwapKeyAppFees(
            totalFees = response.keyAppFees.fee.toBigInteger(),
            signatureFee = feesJson.optLong("signatureFee").toBigInteger(),
            ataDepositsInSol = ataDeposits,
            platformFeeTokenB = platformFeeAmount,
            platformFeePercent = platformFeePercent,
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

    private fun getAtaDepositsSum(feesJson: JSONObject): BigInteger {
        return feesJson.optJSONArray("ataDeposits")
            ?.mapAsStrings { it.toBigInteger() }
            ?.sumOf { it }
            .orZero()
    }

    private fun getPlatformFeePercent(fee: SwapJupiterV6QuoteResponse.PlatformFeeResponse?): BigDecimal {
        return fee?.feeBps?.divideSafe(100.toBigDecimal()).orZero()
    }
}
