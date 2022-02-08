package org.p2p.wallet.send.model

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.toUsd
import java.math.BigDecimal

sealed class SendFee(
    open val fee: BigDecimal,
    open val feePayerToken: Token.Active
) {

    val feeUsd: BigDecimal?
        get() = fee.toUsd(feePayerToken)

    val formattedFee: String
        get() = "${fee.toPlainString()} ${feePayerToken.tokenSymbol}"

    val feePayerSymbol: String
        get() = feePayerToken.tokenSymbol

    fun isFeePayerSame(sourceSymbol: String): Boolean = sourceSymbol == feePayerSymbol

    data class RenBtcFee(
        override val fee: BigDecimal,
        override val feePayerToken: Token.Active
    ) : SendFee(fee, feePayerToken) {

        val fullFee: String
            get() = "$fee $feePayerSymbol ${approxFeeUsd.orEmpty()}"

        val approxFeeUsd: String? get() = fee.toUsd(feePayerToken)?.let { "(~$$it)" }
    }

    data class SolanaFee(
        override val fee: BigDecimal,
        override val feePayerToken: Token.Active
    ) : SendFee(fee, feePayerToken) {

        val accountCreationFullFee: String
            get() = "$fee $feePayerSymbol ${approxAccountCreationFeeUsd.orEmpty()}"

        val approxAccountCreationFeeUsd: String?
            get() = fee.toUsd(feePayerToken)?.let { "(~$$it)" }
    }
}