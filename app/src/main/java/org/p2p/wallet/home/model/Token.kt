package org.p2p.wallet.home.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.AmountUtils
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.Constants.SOL_NAME
import org.p2p.wallet.utils.Constants.USDC_SYMBOL
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toPowerValue
import java.math.BigDecimal

sealed class Token constructor(
    open val publicKey: String?,
    open val tokenSymbol: String,
    open val decimals: Int,
    open val mintAddress: String,
    open val tokenName: String,
    open val iconUrl: String?,
    open val serumV3Usdc: String?,
    open val serumV3Usdt: String?,
    open val isWrapped: Boolean,
    open val usdRate: BigDecimal?
) : Parcelable {

    @Parcelize
    data class Active(
        override val publicKey: String,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val visibility: TokenVisibility,
        override val usdRate: BigDecimal?,
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val iconUrl: String?,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean
    ) : Token(
        publicKey = publicKey,
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        iconUrl = iconUrl,
        serumV3Usdc = serumV3Usdc,
        serumV3Usdt = serumV3Usdt,
        isWrapped = isWrapped,
        usdRate = usdRate
    ) {

        @IgnoredOnParcel
        val isZero: Boolean
            get() = total.isZero()

        @IgnoredOnParcel
        val usdRateOrZero: BigDecimal
            get() = usdRate ?: BigDecimal.ZERO

        fun isDefinitelyHidden(isZerosHidden: Boolean): Boolean =
            visibility == TokenVisibility.HIDDEN || isZerosHidden && isZero && visibility == TokenVisibility.DEFAULT

        fun getFormattedUsdTotal(): String? = totalInUsd?.let { "$${totalInUsd.scaleShort()}" }

        fun getFormattedTotal(includeSymbol: Boolean = false): String =
            if (includeSymbol) {
                "${AmountUtils.format(total)} $tokenSymbol"
            } else {
                AmountUtils.format(total)
            }

        fun getTotal(includeSymbol: Boolean = false): String =
            if (includeSymbol) {
                "${total.scaleLong().toPlainString()} $tokenSymbol"
            } else {
                total.scaleLong().toPlainString()
            }

        fun getVisibilityIcon(isZerosHidden: Boolean): Int {
            return if (isDefinitelyHidden(isZerosHidden)) {
                R.drawable.ic_show
            } else {
                R.drawable.ic_hide
            }
        }
    }

    @Parcelize
    data class Other(
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val iconUrl: String?,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean,
        override val usdRate: BigDecimal?
    ) : Token(
        publicKey = null,
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        iconUrl = iconUrl,
        serumV3Usdc = serumV3Usdc,
        serumV3Usdt = serumV3Usdt,
        isWrapped = isWrapped,
        usdRate = usdRate
    )

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = mintAddress == WRAPPED_SOL_MINT

    @IgnoredOnParcel
    val isRenBTC: Boolean
        get() = tokenSymbol == REN_BTC_SYMBOL

    @IgnoredOnParcel
    val isUSDC: Boolean
        get() = tokenSymbol == USDC_SYMBOL

    fun getFormattedName(): String = if (isSOL) SOL_NAME else tokenName

    companion object {
        fun createSOL(
            publicKey: String,
            tokenData: TokenData,
            amount: Long,
            exchangeRate: BigDecimal?
        ): Active {
            val total: BigDecimal = BigDecimal(amount).divide(tokenData.decimals.toPowerValue())
            return Active(
                publicKey = publicKey,
                tokenSymbol = tokenData.symbol,
                decimals = tokenData.decimals,
                mintAddress = tokenData.mintAddress,
                tokenName = SOL_NAME,
                iconUrl = tokenData.iconUrl,
                totalInUsd = exchangeRate?.let { total.multiply(it) },
                total = total,
                usdRate = exchangeRate,
                visibility = TokenVisibility.SHOWN,
                serumV3Usdc = tokenData.serumV3Usdc,
                serumV3Usdt = tokenData.serumV3Usdt,
                isWrapped = tokenData.isWrapped
            )
        }
    }
}
