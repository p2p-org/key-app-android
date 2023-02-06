package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.swap.jupiter.repository.model.SwapFees
import org.p2p.wallet.swap.jupiter.repository.model.SwapMarketInformation
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.toBase58Instance

class JupiterSwapRoutesMapper {

    fun toSwapRoute(response: SwapJupiterQuoteResponse): List<SwapRoute> = response.routes
        .map { route ->
            SwapRoute(
                inAmountInLamports = route.inAmount.toBigInteger(),
                outAmountInLamports = route.outAmount.toBigInteger(),
                priceImpactPct = route.priceImpactPct.toBigDecimal(),
                marketInfos = route.marketInfos.toSwapMarketInformation(),
                amountInLamports = route.amount.toBigInteger(),
                slippageBps = route.slippageBps,
                otherAmountThreshold = route.otherAmountThreshold,
                swapMode = route.swapMode,
                fees = route.fees.toSwapFee()
            )
        }

    private fun List<SwapRouteRequest.MarketInfoRequest>.toSwapMarketInformation(): List<SwapMarketInformation> =
        map { response ->
            SwapMarketInformation(
                id = response.id,
                label = response.label,
                inputMint = response.inputMint.toBase58Instance(),
                outputMint = response.outputMint.toBase58Instance(),
                notEnoughLiquidity = response.notEnoughLiquidity,
                inAmount = response.inAmount.toBigInteger(),
                outAmount = response.outAmount.toBigInteger(),
                minInAmount = response.minInAmount?.toBigInteger(),
                minOutAmount = response.minOutAmount?.toBigInteger(),
                priceImpactPct = response.priceImpactPct.toBigDecimal(),
                lpFee = response.lpFee.let { responseFee ->
                    SwapMarketInformation.LpFee(
                        amountInLamports = responseFee.amount.toBigInteger(),
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct.toBigDecimal()
                    )
                },
                platformFee = response.platformFee.let { responseFee ->
                    SwapMarketInformation.PlatformFeeRequest(
                        amountInLamports = responseFee.amountInLamports.toBigInteger(),
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct.toBigDecimal()
                    )
                }
            )
        }

    private fun JupiterSwapFeesRequest.toSwapFee(): SwapFees = SwapFees(
        signatureFee = signatureFeeInLamports.toBigInteger(),
        openOrdersDeposits = openOrdersDepositsLamports.map { it.toBigInteger() },
        ataDeposits = ataDeposits.map { it.toBigInteger() },
        totalFeeAndDeposits = totalFeeAndDepositsLamports.toBigInteger(),
        minimumSolForTransaction = minimumSolForTransactionLamports.toBigInteger(),
    )
}
