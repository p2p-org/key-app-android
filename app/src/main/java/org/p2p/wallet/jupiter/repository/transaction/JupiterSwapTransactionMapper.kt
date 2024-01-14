package org.p2p.wallet.jupiter.repository.transaction

import org.p2p.core.crypto.Base64String
import org.p2p.wallet.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapFees
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMarketInformation
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.api.request.CreateSwapTransactionV6Request
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6

class JupiterSwapTransactionMapper {

    fun toNetwork(route: JupiterSwapRoute, userPublicKey: Base58String): CreateSwapTransactionRequest {
        return route.toRequest(userPublicKey)
    }

    fun toNetwork(route: JupiterSwapRouteV6, userPublicKey: Base58String): CreateSwapTransactionV6Request = route.run {
        return CreateSwapTransactionV6Request(
            route = route.originalRoute,
            userPublicKey = userPublicKey
        )
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
                minInAmount = domainModel.minInAmountInLamports?.toString(),
                minOutAmount = domainModel.minOutAmountInLamports?.toString(),
                priceImpactPct = domainModel.priceImpactPct.toDouble(),
                lpFee = domainModel.liquidityFee.let { domainFee ->
                    SwapRouteRequest.MarketInfoRequest.LpFeeRequest(
                        amount = domainFee.amountInLamports.toString(),
                        mint = domainFee.mint.base58Value,
                        pct = domainFee.percent.toDouble()
                    )
                },
                platformFee = domainModel.platformFee.let { domainFee ->
                    SwapRouteRequest.MarketInfoRequest.PlatformFeeRequest(
                        amountInLamports = domainFee.amountInLamports.toString(),
                        mint = domainFee.mint.base58Value,
                        pct = domainFee.percent.toDouble()
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
                fees = fees.toRequest(),
                keyAppFees = SwapRouteRequest.KeyAppFees(
                    fee = keyAppFeeInLamports.toString(),
                    refundableFee = keyAppRefundableFee,
                    hash = keyAppHash
                )
            ),
            userPublicKey = userPublicKey
        )
    }

    private fun JupiterSwapFees.toRequest(): JupiterSwapFeesRequest {
        return JupiterSwapFeesRequest(
            signatureFeeInLamports = signatureFee.toLong(),
            openOrdersDepositsLamports = openOrdersDeposits.map { it.toLong() },
            ataDeposits = ataDeposits.map { it.toLong() },
            totalFeeAndDepositsLamports = totalFeeAndDepositsInSol.toLong(),
            minimumSolForTransactionLamports = minimumSolForTransaction.toLong()
        )
    }
}
