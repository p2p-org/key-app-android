package org.p2p.wallet.feerelayer.model

import org.p2p.core.utils.orZero
import org.p2p.wallet.feerelayer.api.TransactionFeeLimitsResponse

object FeeRelayerLimitsConverter {

    fun fromNetwork(response: TransactionFeeLimitsResponse): TransactionFeeLimits {
        val limits = response.limits
        val processedFee = response.processedFee

        return TransactionFeeLimits(
            limits = Limits(
                maxFeeAmountAllowed = limits.maxFeeAmount.orZero(),
                maxFeeCountAllowed = limits.maxFeeCount.orZero(),
                maxAccountCreationAmountAllowed = limits.maxAccountCreationAmount.orZero(),
                maxAccountCreationCountAllowed = limits.maxAccountCreationCount.orZero()
            ),
            processedFee = ProcessedFee(
                totalFeeAmountUsed = processedFee.totalFeeAmount.orZero(),
                totalRentAmountUsed = processedFee.totalRentAmount.orZero(),
                totalFeeCountUsed = processedFee.feeCount.orZero(),
                totalRentCountUsed = processedFee.rentCount.orZero(),
                totalAmountUsed = processedFee.totalAmount.orZero()
            )
        )
    }
}
