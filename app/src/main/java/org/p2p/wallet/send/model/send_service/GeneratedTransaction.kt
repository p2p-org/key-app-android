package org.p2p.wallet.send.model.send_service

import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.wrapper.eth.helpers.RandomHelper.randomBytes

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
    ) : Parcelable {
        companion object {
            val EMPTY = AmountData(
                amount = BigInteger.ZERO,
                usdAmount = BigDecimal.ZERO,
                address = randomBytes(32).toBase58Instance(),
                tokenSymbol = "",
                tokenName = "",
                decimals = 0,
                logoUrl = null,
                coingeckoId = null,
                price = emptyMap(),
            )
        }
    }

    @Parcelize
    data class NetworkFeeData(
        val source: NetworkFeeSource,
        val amount: AmountData,
    ) : Parcelable
}
