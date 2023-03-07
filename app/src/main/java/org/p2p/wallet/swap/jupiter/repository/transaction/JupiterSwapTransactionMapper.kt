package org.p2p.wallet.swap.jupiter.repository.transaction

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapFees
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapMarketInformation
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

class JupiterSwapTransactionMapper {

    fun toNetwork(route: JupiterSwapRoute, userPublicKey: Base58String): CreateSwapTransactionRequest {
        return route.toRequest(userPublicKey)
    }

    fun fromNetwork(response: CreateSwapTransactionResponse): Base64String {
        return response.versionedSwapTransaction
    }

    private fun List<JupiterSwapMarketInformation>.toRequest(): List<SwapRouteRequest.MarketInfoRequest> =
        map { domainModel ->
            SwapRouteRequest.MarketInfoRequest(
                id = domainModel.id,
                label = domainModel.label,
                inputMint = domainModel.inputMint.base58Value,
                outputMint = domainModel.outputMint.base58Value,
                notEnoughLiquidity = domainModel.notEnoughLiquidity,
                inAmount = domainModel.inAmountInLamports.toString(),
                outAmount = domainModel.outAmountInLamports.toString(),
                minInAmount = domainModel.minInAmountInLamports.toString(),
                minOutAmount = domainModel.minOutAmountInLamports.toString(),
                priceImpactPct = domainModel.priceImpactPct.toDouble(),
                lpFee = domainModel.lpFee.let { domainFee ->
                    SwapRouteRequest.MarketInfoRequest.LpFeeRequest(
                        amount = domainFee.amountInLamports.toString(),
                        mint = domainFee.mint.base58Value,
                        pct = domainFee.pct.toDouble()
                    )
                },
                platformFee = domainModel.platformFee.let { domainFee ->
                    SwapRouteRequest.MarketInfoRequest.PlatformFeeRequest(
                        amountInLamports = domainFee.amountInLamports.toString(),
                        mint = domainFee.mint.base58Value,
                        pct = domainFee.pct.toDouble()
                    )
                }
            )
        }

    private fun JupiterSwapRoute.toRequest(userPublicKey: Base58String): CreateSwapTransactionRequest {
        return CreateSwapTransactionRequest(
            route = SwapRouteRequest(
                inAmount = inAmountInLamports.toString(),
                outAmount = outAmountInLamports.toString(),
                priceImpactPct = priceImpactPct.toDouble(),
                marketInfos = marketInfos.toRequest(),
                amount = amountInLamports.toString(),
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

    private fun JupiterSwapFees.toRequest(): JupiterSwapFeesRequest {
        return JupiterSwapFeesRequest(
            signatureFeeInLamports = signatureFee.toLong(),
            openOrdersDepositsLamports = openOrdersDeposits.map { it.toLong() },
            ataDeposits = ataDeposits.map { it.toLong() },
            totalFeeAndDepositsLamports = totalFeeAndDeposits.toLong(),
            minimumSolForTransactionLamports = minimumSolForTransaction.toLong()
        )
    }
}
