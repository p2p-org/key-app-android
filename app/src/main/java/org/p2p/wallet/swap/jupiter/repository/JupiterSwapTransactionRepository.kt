package org.p2p.wallet.swap.jupiter.repository

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.repository.model.SwapFailure
import org.p2p.wallet.swap.jupiter.repository.model.SwapFees
import org.p2p.wallet.swap.jupiter.repository.model.SwapMarketInformation
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.LamportsAmount
import kotlinx.coroutines.withContext

class JupiterSwapTransactionRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun createSwapTransactionForRoute(route: SwapRoute, userPublicKey: Base58String): Base64String =
        withContext(dispatchers.io) {
            try {
                val request = route.toRequest(userPublicKey)
                api.createRouteSwapTransaction(request).swapTransaction
            } catch (error: Throwable) {
                throw SwapFailure.CreateSwapTransactionFailed(route, error)
            }
        }

    private fun List<SwapMarketInformation>.toRequest(): List<SwapRouteRequest.MarketInfoRequest> = map { domainModel ->
        SwapRouteRequest.MarketInfoRequest(
            id = domainModel.id,
            label = domainModel.label,
            inputMint = domainModel.inputMint.base58Value,
            outputMint = domainModel.outputMint.base58Value,
            notEnoughLiquidity = domainModel.notEnoughLiquidity,
            inAmount = domainModel.inAmount.toPlainString(),
            outAmount = domainModel.outAmount.toPlainString(),
            minInAmount = domainModel.minInAmount?.toPlainString(),
            minOutAmount = domainModel.minOutAmount?.toPlainString(),
            priceImpactPct = domainModel.priceImpactPct,
            lpFee = domainModel.lpFee.let { domainFee ->
                SwapRouteRequest.MarketInfoRequest.LpFeeRequest(
                    amount = domainFee.amount,
                    mint = domainFee.mint.base58Value,
                    pct = domainFee.pct
                )
            },
            platformFee = domainModel.platformFee.let { domainFee ->
                SwapRouteRequest.MarketInfoRequest.PlatformFeeRequest(
                    amount = domainFee.amount,
                    mint = domainFee.mint.base58Value,
                    pct = domainFee.pct
                )
            }
        )
    }

    private fun SwapRoute.toRequest(userPublicKey: Base58String): CreateSwapTransactionRequest {
        return CreateSwapTransactionRequest(
            route = SwapRouteRequest(
                inAmount = inAmount.toPlainString(),
                outAmount = outAmount.toPlainString(),
                priceImpactPct = priceImpactPct.toDouble(),
                marketInfos = marketInfos.toRequest(),
                amount = amount.toPlainString(),
                slippageBps = slippageBps,
                otherAmountThreshold = otherAmountThreshold,
                swapMode = swapMode,
                fees = fees.toRequest()
            ),
            userPublicKey = userPublicKey,
            wrapUnwrapSOL = true,
            asLegacyTransaction = true
        )
    }

    private fun SwapFees.toRequest(): JupiterSwapFeesRequest {
        return JupiterSwapFeesRequest(
            signatureFeeInLamports = signatureFee.valueAsDouble,
            openOrdersDepositsLamports = openOrdersDeposits.map(LamportsAmount::valueAsDouble),
            ataDeposits = ataDeposits.map(LamportsAmount::valueAsDouble),
            totalFeeAndDepositsLamports = totalFeeAndDeposits.valueAsDouble,
            minimumSolForTransactionLamports = minimumSolForTransaction.valueAsDouble
        )
    }
}
