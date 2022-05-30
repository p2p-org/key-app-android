package org.p2p.wallet.send.model

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toUsd
import java.math.BigDecimal
import java.math.BigInteger

sealed class SendFee(
    open val fee: BigDecimal,
    open val feePayerToken: Token.Active,
    open val sourceTokenSymbol: String
) {

    val feeLamports: BigInteger
        get() = fee.toLamports(feePayerToken.decimals)

    val feeTotalLamports: BigInteger
        get() = feePayerToken.total.toLamports(feePayerToken.decimals)

    val feeUsd: BigDecimal?
        get() = fee.toUsd(feePayerToken)

    val formattedFee: String
        get() = "${fee.toPlainString()} ${feePayerToken.tokenSymbol}"

    val feePayerSymbol: String
        get() = feePayerToken.tokenSymbol

    data class RenBtcFee(
        override val fee: BigDecimal,
        override val feePayerToken: Token.Active,
        override val sourceTokenSymbol: String
    ) : SendFee(fee, feePayerToken, sourceTokenSymbol) {

        val fullFee: String
            get() = "$fee $feePayerSymbol ${approxFeeUsd.orEmpty()}"

        val approxFeeUsd: String? get() = fee.toUsd(feePayerToken)?.let { "(~$$it)" }
    }

    data class SolanaFee(
        override val fee: BigDecimal,
        override val feePayerToken: Token.Active,
        override val sourceTokenSymbol: String
    ) : SendFee(fee, feePayerToken, sourceTokenSymbol) {

        val accountCreationFullFee: String
            get() = "$fee $feePayerSymbol ${approxAccountCreationFeeUsd.orEmpty()}"

        val approxAccountCreationFeeUsd: String?
            get() = fee.toUsd(feePayerToken)?.let { "(~$$it)" }
    }
}
