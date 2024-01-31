package org.p2p.wallet.send.model.send_service

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String

data class GeneratedTransaction(
    val transaction: Base64String,
    val blockhash: Base58String,
    val expiresAt: Long,
    val signature: String,
    val recipientGetsAmount: AmountData,
    val totalAmount: AmountData,
    val networkFee: NetworkFeeData,
    val tokenAccountRent: NetworkFeeData? = null,
    val token2022TransferFee: NetworkFeeData? = null,
) {
    data class AmountData(
        val amount: BigInteger,
        val usdAmount: BigDecimal,
        val address: Base58String,
        val tokenSymbol: String,
        val tokenName: String,
        val decimals: Int,
        val logoUrl: String?,
        val coingeckoId: String?,
        val price: Map<String, BigDecimal>,
    )

    data class NetworkFeeData(
        val source: NetworkFeeSource,
        val amount: AmountData,
    )
}
