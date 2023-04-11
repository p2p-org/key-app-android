package org.p2p.wallet.feerelayer.model

import org.p2p.core.utils.orZero
import org.p2p.wallet.feerelayer.api.TransactionFeeLimitsResponse

object FeeRelayerLimitsConverter {

    fun fromNetwork(response: TransactionFeeLimitsResponse): TransactionFeeLimits {

        val limits = response.limits
        val processedFee = response.processedFee

        return TransactionFeeLimits(
            maxFeeCountAllowed = limits.maxFeeCount.orZero(),
            maxFeeAmountAllowed = limits.maxFeeAmount.orZero(),
            overallFeeCountUsed = processedFee.feeCount.orZero(),
            overallFeeAmountUsed = processedFee.totalFeeAmount.orZero(),

            maxTransactionsAllowed = limits.maxCount,
            maxTransactionsAmountAllowed = limits.maxAmount,
            transactionsUsed = processedFee.count,
            totalFeeAmountUsed = processedFee.totalAmount,

            maxAccountCreationCount = limits.maxAccountCreationCount.orZero(),
            maxAccountCreationAmount = limits.maxAccountCreationAmount.orZero(),
            accountCreationUsed = processedFee.rentCount.orZero(),
            accountCreationAmountUsed = processedFee.totalRentAmount.orZero()
        )
    }
}
