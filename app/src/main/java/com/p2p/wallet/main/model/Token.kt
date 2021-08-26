package com.p2p.wallet.main.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.p2p.wallet.R
import com.p2p.wallet.user.model.TokenData
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleShort
import com.p2p.wallet.utils.toPowerValue
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class Token constructor(
    val publicKey: String,
    val tokenSymbol: String,
    val decimals: Int,
    val mintAddress: String,
    val tokenName: String,
    val logoUrl: String?,
    val price: BigDecimal,
    val total: BigDecimal,
    @ColorRes val color: Int,
    val usdRate: BigDecimal,
    val visibility: TokenVisibility,
    val isWrapped: Boolean
) : Parcelable {

    @IgnoredOnParcel
    val isZero: Boolean
        get() = total.isZero()

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = tokenSymbol == SOL_SYMBOL

    @IgnoredOnParcel
    val isUSDC: Boolean
        get() = tokenSymbol == USDC_SYMBOL

    @IgnoredOnParcel
    val totalInUsd: BigDecimal
        get() = total.multiply(usdRate).scaleLong()

    fun getVisibilityIcon(isZerosHidden: Boolean): Int {
        return if (isDefinitelyHidden(isZerosHidden)) {
            R.drawable.ic_show
        } else {
            R.drawable.ic_hide
        }
    }

    fun isDefinitelyHidden(isZerosHidden: Boolean): Boolean =
        visibility == TokenVisibility.HIDDEN || isZerosHidden && isZero && visibility == TokenVisibility.DEFAULT

    @Suppress("MagicNumber")
    fun getFormattedAddress(): String {
        if (!isSOL) return tokenName

        val firstSix = publicKey.take(4)
        val lastFour = publicKey.takeLast(4)
        return "$firstSix...$lastFour"
    }

    fun getFormattedPrice(): String = "${price.scaleShort()} $"

    fun getFormattedTotal(): String = "$total $tokenSymbol"

    fun getCurrentPrice(): String = "${String.format("%.2f", usdRate)} per $tokenSymbol"

    companion object {
        const val USD_SYMBOL = "USD"
        const val SOL_SYMBOL = "SOL"
        const val USDC_SYMBOL = "USDC"
        const val SOL_MINT = "So11111111111111111111111111111111111111112"

        fun createSOL(
            publicKey: String,
            tokenData: TokenData,
            amount: Long,
            exchangeRate: BigDecimal
        ): Token {
            val total: BigDecimal = BigDecimal(amount).divide(tokenData.decimals.toPowerValue())
            return Token(
                publicKey = publicKey,
                tokenSymbol = tokenData.symbol,
                decimals = tokenData.decimals,
                mintAddress = tokenData.mintAddress,
                tokenName = tokenData.name,
                logoUrl = tokenData.iconUrl,
                price = total.multiply(exchangeRate),
                total = total,
                color = R.color.chartSOL,
                usdRate = exchangeRate,
                visibility = TokenVisibility.SHOWN,
                isWrapped = tokenData.isWrapped
            )
        }
    }
}