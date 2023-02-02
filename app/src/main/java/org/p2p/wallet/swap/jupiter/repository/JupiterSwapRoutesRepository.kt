package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.api.response.quote.SwapJupiterQuoteResponse
import org.p2p.wallet.swap.jupiter.repository.model.SwapFees
import org.p2p.wallet.swap.jupiter.repository.model.SwapMarketInformation
import org.p2p.wallet.swap.jupiter.repository.model.SwapQuote
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.toLamportsInstance
import kotlinx.coroutines.withContext

class JupiterSwapRoutesRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
) : SwapRoutesRepository {

    override suspend fun getSwapQuote(swapQuote: SwapQuote, userPublicKey: Base58String): List<SwapRoute> =
        withContext(dispatchers.io) {
            api.getQuote(swapQuote.inputMint, swapQuote.outputMint, swapQuote.amount, userPublicKey)
                .toSwapRoute()
        }

    private fun SwapJupiterQuoteResponse.toSwapRoute(): List<SwapRoute> = routes
        .map { route ->
            SwapRoute(
                inAmount = route.inAmount.toBigDecimal(),
                outAmount = route.outAmount.toBigDecimal(),
                priceImpactPct = route.priceImpactPct.toBigDecimal(),
                marketInfos = route.marketInfos.toSwapMarketInformation(),
                amount = route.amount.toBigDecimal(),
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
                inAmount = response.inAmount.toBigDecimal(),
                outAmount = response.outAmount.toBigDecimal(),
                minInAmount = response.minInAmount?.toBigDecimal(),
                minOutAmount = response.minOutAmount?.toBigDecimal(),
                priceImpactPct = response.priceImpactPct,
                lpFee = response.lpFee.let { responseFee ->
                    SwapMarketInformation.LpFee(
                        amount = responseFee.amount,
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct
                    )
                },
                platformFee = response.platformFee.let { responseFee ->
                    SwapMarketInformation.PlatformFeeRequest(
                        amount = responseFee.amount,
                        mint = responseFee.mint.toBase58Instance(),
                        pct = responseFee.pct
                    )
                }
            )
        }

    private fun JupiterSwapFeesRequest.toSwapFee(): SwapFees = SwapFees(
        signatureFee = signatureFeeInLamports.toLamportsInstance(),
        openOrdersDeposits = openOrdersDepositsLamports.map { it.toLamportsInstance() },
        ataDeposits = ataDeposits.map { it.toLamportsInstance() },
        totalFeeAndDeposits = totalFeeAndDepositsLamports.toLamportsInstance(),
        minimumSolForTransaction = minimumSolForTransactionLamports.toLamportsInstance(),
    )
}
