package org.p2p.wallet.send.model

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.toUsd
import java.math.BigDecimal

data class SendFee(
    val fee: BigDecimal,
    val feePayerToken: Token.Active
) {

    val feeUsd: BigDecimal?
        get() = fee.toUsd(feePayerToken)

    val formattedFee: String
        get() = "${fee.toPlainString()} ${feePayerToken.tokenSymbol}"
}