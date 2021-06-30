package com.p2p.wallet.token.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.p2p.wallet.R
import com.p2p.wallet.amount.isZero
import com.p2p.wallet.amount.scalePrice
import com.p2p.wallet.amount.toPowerValue
import com.p2p.wallet.common.network.Constants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

@Parcelize
data class Token constructor(
    val tokenSymbol: String,
    val publicKey: String,
    val decimals: Int,
    val mintAddress: String,
    val tokenName: String,
    val logoUrl: String?,
    val price: BigDecimal,
    val total: BigDecimal,
    @ColorRes val color: Int,
    val usdRate: BigDecimal,
    val visibility: TokenVisibility
) : Parcelable {

    @IgnoredOnParcel
    val isHidden: Boolean
        get() = visibility == TokenVisibility.HIDDEN

    @IgnoredOnParcel
    val isZero: Boolean
        get() = total.isZero()

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = tokenName == SOL_NAME

    @IgnoredOnParcel
    val totalInUsd: BigDecimal
        get() = total.multiply(usdRate).scalePrice()

    fun getVisibilityIcon(isZerosHidden: Boolean): Int {
        val shouldBeHidden = isZerosHidden && visibility == TokenVisibility.DEFAULT && isZero
        return if (visibility == TokenVisibility.HIDDEN || shouldBeHidden) {
            R.drawable.ic_show
        } else {
            R.drawable.ic_hide
        }
    }

    fun getFormattedMintAddress(): String = if (mintAddress == SOL_MINT) {
        Constants.WRAPPED_SOL_MINT
    } else {
        mintAddress
    }

    @Suppress("MagicNumber")
    fun getFormattedAddress(): String {
        if (publicKey.length < ADDRESS_SYMBOL_COUNT) {
            return publicKey
        }

        val firstSix = publicKey.take(4)
        val lastFour = publicKey.takeLast(4)
        return "$firstSix...$lastFour"
    }

    fun getFormattedPrice(): String = "${price.setScale(2, RoundingMode.HALF_EVEN)} $"

    fun getFormattedTotal(): String = "$total $tokenSymbol"

    fun getFormattedExchangeRate(): String = String.format("%.2f", usdRate)

    companion object {
        const val USD_SYMBOL = "USD"
        private const val ADDRESS_SYMBOL_COUNT = 10
        private const val SOL_DECIMALS = 9
        private const val SOL_MINT = "So11111111111111111111111111111111111111112"
        private const val SOL_NAME = "SOL"
        private const val SOL_LOGO_URL =
            "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/solana/info/logo.png"

        /* fixme: workaround about adding hardcode wallet, looks strange */
        fun getSOL(publicKey: String, amount: Long) = Token(
            tokenSymbol = SOL_NAME,
            tokenName = SOL_NAME,
            mintAddress = SOL_MINT,
            logoUrl = SOL_LOGO_URL,
            publicKey = publicKey,
            decimals = SOL_DECIMALS,
            total = BigDecimal(amount).divide(SOL_DECIMALS.toPowerValue()),
            price = BigDecimal.ZERO,
            color = R.color.chartSOL,
            usdRate = BigDecimal.ZERO,
            visibility = TokenVisibility.SHOWN
        )
    }
}