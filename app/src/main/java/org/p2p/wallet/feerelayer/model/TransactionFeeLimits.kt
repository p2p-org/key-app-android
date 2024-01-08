package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * The user is allowed to make 100 transactions where FR (FeeRelayer) pays for transaction fees.
 * Within these 100 transactions, the user is allowed to create 30 links where FR pays for rent.
 * In this class we count both the [count] and [amount] fields to validate if user has free transactions
 *
 *    {
 *        "limits": {
 *           "use_free_fee": true,
 *           "max_fee_amount": 10000000,
 *           "max_fee_count": 100,
 *           "max_token_account_creation_amount": 61200000,
 *           "max_token_account_creation_count": 30,
 *           "max_transaction_lifetime": {
 *           "secs": 360,
 *           "nanos": 0
 *        },
 *        "period": {
 *           "secs": 86400,
 *           "nanos": 0
 *        },
 *           "max_amount": 10000000,
 *           "max_count": 100
 *        },
 *        "processed_fee": {
 *            "total_fee_amount": 110000,
 *            "total_rent_amount": 12235680,
 *            "fee_count": 11,
 *            "rent_count": 6,
 *            "count": 11,
 *            "total_amount": 12345680
 *        }
 *   }
 * */

@Parcelize
data class TransactionFeeLimits(
    private val limits: Limits,
    private val processedFee: ProcessedFee
) : Parcelable {

    @IgnoredOnParcel
    val remaining = limits.maxFeeCountAllowed - processedFee.totalFeeCountUsed

    fun isTransactionAllowed(): Boolean {
        val isCountEnough = limits.maxFeeCountAllowed > processedFee.totalFeeCountUsed
        val isAmountEnough = limits.maxFeeAmountAllowed > processedFee.totalFeeAmountUsed
        return isCountEnough && isAmountEnough
    }

    fun isSendViaLinkAllowed(): Boolean = isAccountCreationAllowed() && isTransactionAllowed()

    fun isFreeTransactionFeeAvailable(transactionFee: BigInteger, forNextTransaction: Boolean = false): Boolean {
        var totalFeeCountUsed = processedFee.totalFeeCountUsed
        if (forNextTransaction) {
            totalFeeCountUsed += 1
        }
        val totalFee = processedFee.totalFeeAmountUsed + transactionFee
        return totalFeeCountUsed < limits.maxFeeCountAllowed && totalFee <= limits.maxFeeAmountAllowed
    }

    private fun isAccountCreationAllowed(): Boolean {
        val maxAccountCreationCountAllowed = limits.maxAccountCreationCountAllowed
        val maxAccountCreationAmountAllowed = limits.maxAccountCreationAmountAllowed

        val totalRentCountUsed = processedFee.totalRentCountUsed
        val totalRentAmountUsed = processedFee.totalRentAmountUsed
        val isAccountCreationCountEnough = maxAccountCreationCountAllowed > totalRentCountUsed
        val isAccountCreationAmountEnough = maxAccountCreationAmountAllowed > totalRentAmountUsed

        return isAccountCreationCountEnough && isAccountCreationAmountEnough
    }
}

@Parcelize
data class Limits(
    val maxFeeCountAllowed: Int,
    val maxFeeAmountAllowed: BigInteger,
    val maxAccountCreationCountAllowed: Int,
    val maxAccountCreationAmountAllowed: BigInteger,
) : Parcelable

@Parcelize
data class ProcessedFee(
    val totalFeeAmountUsed: BigInteger,
    val totalRentAmountUsed: BigInteger,
    val totalFeeCountUsed: Int,
    val totalRentCountUsed: Int,
    val totalAmountUsed: BigInteger
) : Parcelable
