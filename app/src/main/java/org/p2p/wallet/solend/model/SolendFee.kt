package org.p2p.wallet.solend.model

import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZeroOrLess
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleShort
import java.math.BigDecimal
import java.math.BigInteger

data class SolendFee constructor(
    val tokenSymbol: String,
    val usdRate: BigDecimal,
    val decimals: Int,
    val fee: SolendTokenFee,
    val feePayer: TokenAccount
) {

    fun getTransferFee(): String? {
        val transferFee = getTransferFeeInDecimals()
        val transferFeeUsd = getTransferFeeInUsd()

        return if (!transferFee.isZeroOrLess()) {
            "$transferFee $tokenSymbol (~$ $transferFeeUsd)"
        } else {
            null
        }
    }

    fun getRentFee(): String {
        val rentFee = getRentFeeInDecimals()
        val rentFeeUsd = getRentFeeInUsd()

        return if (rentFee.isZeroOrLess()) {
            "${BigDecimal.ZERO} $tokenSymbol"
        } else {
            "$rentFee $tokenSymbol (~$ $rentFeeUsd)"
        }
    }

    fun getTotalFee(
        currentInput: BigDecimal,
        selectedDepositToken: SolendDepositToken,
        amountInLamports: BigInteger
    ): String {
        var totalInLamports = amountInLamports
        var totalInUsd = (currentInput * selectedDepositToken.usdRate).scaleShort()

        if (selectedDepositToken.tokenSymbol == tokenSymbol) {
            totalInLamports += fee.total
            totalInUsd = totalInLamports.fromLamports(decimals).scaleShort()
        } else {
            val transferFeeUsd = getTransferFeeInUsd()
            val rentFeeUsd = getRentFeeInUsd()
            totalInUsd = totalInUsd + transferFeeUsd + rentFeeUsd
        }

        val total = totalInLamports.fromLamports(decimals).scaleLong()
        return "$total $tokenSymbol (~$ $totalInUsd)"
    }

    private fun getTransferFeeInDecimals(): BigDecimal = if (fee.rent.isNotZero()) {
        fee.transaction
            .fromLamports(decimals)
            .scaleLong()
    } else {
        BigDecimal.ZERO
    }

    private fun getTransferFeeInUsd(): BigDecimal = if (fee.rent.isNotZero()) {
        (getTransferFeeInDecimals() * usdRate).scaleShort()
    } else {
        BigDecimal.ZERO
    }

    private fun getRentFeeInDecimals(): BigDecimal = if (fee.rent.isNotZero()) {
        fee.rent
            .fromLamports(decimals)
            .scaleLong()
    } else {
        BigDecimal.ZERO
    }

    private fun getRentFeeInUsd(): BigDecimal = if (fee.rent.isNotZero()) {
        (getRentFeeInDecimals() * usdRate).scaleShort()
    } else {
        BigDecimal.ZERO
    }
}
