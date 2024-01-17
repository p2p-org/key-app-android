package org.p2p.solanaj.kits

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.isZero

sealed interface AccountInfoTokenExtensionConfig {

    companion object {
        fun Map<String, AccountInfoTokenExtensionConfig>.getTransferFeeConfig(): TransferFeeConfig? {
            return this[TransferFeeConfig.NAME] as? TransferFeeConfig
        }

        fun Map<String, AccountInfoTokenExtensionConfig>.getInterestBearingConfig(): InterestBearingConfig? {
            return this[InterestBearingConfig.NAME] as? InterestBearingConfig
        }
    }

    data class TransferFeeConfig(
        @SerializedName("withheldAmount")
        val withheldAmount: Long,
        @SerializedName("newerTransferFee")
        val newerTransferFee: TransferFeeConfigData? = null,
        @SerializedName("olderTransferFee")
        val olderTransferFee: TransferFeeConfigData? = null
    ) : AccountInfoTokenExtensionConfig {

        companion object {
            const val NAME = "transferFeeConfig"
        }

        data class TransferFeeConfigData(
            val epoch: Int,
            val maximumFee: BigInteger,
            val transferFeeBasisPoints: Int,
        ) {
            val transferFeePercent: BigDecimal
                get() = transferFeeBasisPoints.toBigDecimal().setScale(4)
                    .divideSafe("10000".toBigDecimal().setScale(4))
                    .multiply("100".toBigDecimal().setScale(4))

            fun calculateFee(preFee: BigInteger): BigInteger {
                if (transferFeeBasisPoints == 0 || preFee.isZero()) {
                    return BigInteger.ZERO
                }
                val transferFeeBasisPoints = transferFeeBasisPoints.toBigInteger()
                val numerator = preFee * transferFeeBasisPoints
                val rawFee = numerator.divideSafe(BigInteger("10000"))

                return minOf(rawFee, maximumFee)
            }
        }

        fun getActualTransferFee(currentEpoch: Int): TransferFeeConfigData? {
            if (currentEpoch > (newerTransferFee?.epoch ?: 0) || currentEpoch == 0) {
                return newerTransferFee ?: olderTransferFee
            }
            return olderTransferFee
        }
    }

    data class InterestBearingConfig(
        @SerializedName("currentRate")
        val currentRate: Double,
        @SerializedName("preUpdateAverageRate")
        val preUpdateAverageRate: Double,
        @SerializedName("initializationTimestamp")
        val initializationTimestamp: Long,
        @SerializedName("lastUpdateTimestamp")
        val lastUpdateTimestamp: Long,
        @SerializedName("rateAuthority")
        val rateAuthority: String,
    ) : AccountInfoTokenExtensionConfig {
        companion object {
            const val NAME = "interestBearingConfig"
        }
    }
}

data class AccountInfoTokenExtension(
    /**
     * Possible options:
     * interestBearingConfig
     * transferFeeConfig
     * todo: add more possible options if needed
     */
    @SerializedName("extension")
    val name: String,

    /**
     * Decode this using AccountInfoTokenExtensionConfigDeserializer
     * Actual class must be [AccountInfoTokenExtensionConfig]
     */
    @SerializedName("state")
    val state: JsonElement? = null
)
