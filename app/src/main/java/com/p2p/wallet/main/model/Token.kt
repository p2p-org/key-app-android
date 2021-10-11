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

sealed class Token constructor(
    open val tokenSymbol: String,
    open val decimals: Int,
    open val mintAddress: String,
    open val tokenName: String,
    open val logoUrl: String?,
    @ColorRes open val color: Int,
    open val serumV3Usdc: String?,
    open val serumV3Usdt: String?,
    open val isWrapped: Boolean
) : Parcelable {

    @Parcelize
    data class Active(
        val publicKey: String,
        val price: BigDecimal,
        val total: BigDecimal,
        val visibility: TokenVisibility,
        val usdRate: BigDecimal,
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val logoUrl: String?,
        @ColorRes override val color: Int,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean
    ) : Token(
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        logoUrl = logoUrl,
        color = color,
        serumV3Usdc = serumV3Usdc,
        serumV3Usdt = serumV3Usdt,
        isWrapped = isWrapped
    ) {

        @IgnoredOnParcel
        val isZero: Boolean
            get() = total.isZero()

        @IgnoredOnParcel
        val totalInUsd: BigDecimal
            get() = total.multiply(usdRate).scaleLong()

        fun isDefinitelyHidden(isZerosHidden: Boolean): Boolean =
            visibility == TokenVisibility.HIDDEN || isZerosHidden && isZero && visibility == TokenVisibility.DEFAULT

        fun getCurrentPrice(): String = "${String.format("%.2f", usdRate)} per $tokenSymbol"

        @Suppress("MagicNumber")
        fun getFormattedAddress(): String {
            if (!isSOL) return tokenName

            val firstSix = publicKey.take(4)
            val lastFour = publicKey.takeLast(4)
            return "$firstSix...$lastFour"
        }

        fun getFormattedPrice(): String = "${price.scaleShort()} $"

        fun getFormattedTotal(): String = "$total $tokenSymbol"

        fun getVisibilityIcon(isZerosHidden: Boolean): Int {
            return if (isDefinitelyHidden(isZerosHidden)) {
                R.drawable.ic_show
            } else {
                R.drawable.ic_hide
            }
        }
    }

    @Parcelize
    data class Inactive(
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val logoUrl: String?,
        @ColorRes override val color: Int,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean
    ) : Token(
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        logoUrl = logoUrl,
        color = color,
        serumV3Usdc = serumV3Usdc,
        serumV3Usdt = serumV3Usdt,
        isWrapped = isWrapped
    )

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = tokenSymbol == SOL_SYMBOL

    @IgnoredOnParcel
    val isUSDC: Boolean
        get() = tokenSymbol == USDC_SYMBOL

    @IgnoredOnParcel
    val isRenBTC: Boolean
        get() = tokenSymbol == REN_BTC_SYMBOL

    companion object {
        const val REN_BTC_SYMBOL = "renBTC"
        const val USD_SYMBOL = "USD"
        const val SOL_SYMBOL = "SOL"
        const val USDC_SYMBOL = "USDC"
        const val WRAPPED_SOL_MINT = "So11111111111111111111111111111111111111112"
        const val REN_BTC_DEVNET_MINT = "FsaLodPu4VmSwXGr3gWfwANe4vKf8XSZcCh1CEeJ3jpD"
        const val SOL_MINT =
            "Ejmc1UB4EsES5oAaRN63SpoxMJidt3ZGBrqrZk49vjTZ" // Arbitrary mint to represent SOL (not wrapped SOL).

        fun createSOL(
            publicKey: String,
            tokenData: TokenData,
            amount: Long,
            exchangeRate: BigDecimal
        ): Token.Active {
            val total: BigDecimal = BigDecimal(amount).divide(tokenData.decimals.toPowerValue())
            return Active(
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
                serumV3Usdc = tokenData.serumV3Usdc,
                serumV3Usdt = tokenData.serumV3Usdt,
                isWrapped = tokenData.isWrapped
            )
        }
    }
}