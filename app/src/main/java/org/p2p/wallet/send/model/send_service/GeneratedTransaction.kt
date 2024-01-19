package org.p2p.wallet.send.model.send_service

import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.wrapper.eth.helpers.RandomHelper.randomBytes

data class GeneratedTransaction(
    val transaction: Base64String,
    val blockhash: Base58String,
    val expiresAt: Long,
    val signature: String,
    val recipientGetsAmount: AmountData,
    val totalAmount: AmountData,
    val networkFee: NetworkFeeData,
    val tokenAccountRent: NetworkFeeData,
    val token2022TransferFee: NetworkFeeData,
) {
    @Parcelize
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
    ) : Parcelable

    @Parcelize
    data class NetworkFeeData(
        val source: NetworkFeeSource,
        val amount: AmountData,
    ) : Parcelable {
        companion object {
            val EMPTY = NetworkFeeData(
                source = NetworkFeeSource.User,
                amount = AmountData(
                    amount = BigInteger.ZERO,
                    usdAmount = BigDecimal.ZERO,
                    address = Base58String(randomBytes(32)),
                    tokenSymbol = "",
                    tokenName = "",
                    decimals = 0,
                    logoUrl = null,
                    coingeckoId = null,
                    price = mapOf("usd" to BigDecimal.ZERO),
                )
            )
        }
    }
}
