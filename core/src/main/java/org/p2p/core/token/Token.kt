package org.p2p.core.token

import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.R
import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.core.utils.Constants.SOL_NAME
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_ETH_MINT
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.asCurrency
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toPowerValue
import org.p2p.core.wrapper.eth.EthAddress

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
    open var rate: BigDecimal?,
    open var currency: String = Constants.USD_READABLE_SYMBOL
) : Parcelable {

    @Parcelize
    data class Active constructor(
        override val publicKey: String,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val visibility: TokenVisibility,
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val iconUrl: String?,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean,
        override var rate: BigDecimal?,
        override var currency: String = Constants.USD_READABLE_SYMBOL
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
        rate = rate,
        currency = currency
    ) {

        @IgnoredOnParcel
        val totalInLamports: BigInteger
            get() = total.toLamports(decimals)

        @IgnoredOnParcel
        val totalInUsdScaled: BigDecimal?
            get() = totalInUsd?.scaleShort()

        @IgnoredOnParcel
        val isZero: Boolean
            get() = total.isZero()

        fun isDefinitelyHidden(isZerosHidden: Boolean): Boolean =
            visibility == TokenVisibility.HIDDEN ||
                isZerosHidden &&
                isZero &&
                visibility == TokenVisibility.DEFAULT

        fun getFormattedUsdTotal(includeSymbol: Boolean = true): String? {
            return if (includeSymbol) totalInUsd?.asUsd() else totalInUsd?.formatFiat()
        }

        fun getFormattedTotal(includeSymbol: Boolean = false): String =
            if (includeSymbol) {
                "${total.formatToken(decimals)} $tokenSymbol"
            } else {
                total.formatToken(decimals)
            }

        fun getVisibilityIcon(isZerosHidden: Boolean): Int =
            if (isDefinitelyHidden(isZerosHidden)) {
                R.drawable.ic_show
            } else {
                R.drawable.ic_hide
            }
    }

    @Parcelize
    data class Eth constructor(
        override val publicKey: String,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val iconUrl: String?,
        override var rate: BigDecimal?,
        override var currency: String = Constants.USD_READABLE_SYMBOL
    ) : Token(
        publicKey = publicKey,
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        iconUrl = iconUrl,
        serumV3Usdc = null,
        serumV3Usdt = null,
        isWrapped = false,
        rate = rate,
        currency = currency
    ) {

        @IgnoredOnParcel
        val isEth: Boolean
            get() = mintAddress == WRAPPED_ETH_MINT

        @IgnoredOnParcel
        val totalInLamports: BigInteger
            get() = total.toLamports(decimals)

        @IgnoredOnParcel
        val isZero: Boolean
            get() = total.isZero()

        fun getFormattedUsdTotal(includeSymbol: Boolean = true): String? {
            return if (includeSymbol) totalInUsd?.asUsd() else totalInUsd?.formatFiat()
        }

        fun getFormattedTotal(includeSymbol: Boolean = false): String =
            if (includeSymbol) {
                "${total.formatToken(decimals)} $tokenSymbol"
            } else {
                total.formatToken(decimals)
            }

        fun getEthAddress(): EthAddress {
            return EthAddress(publicKey)
        }
    }

    @Parcelize
    data class Other constructor(
        override val tokenSymbol: String,
        override val decimals: Int,
        override val mintAddress: String,
        override val tokenName: String,
        override val iconUrl: String?,
        override val serumV3Usdc: String?,
        override val serumV3Usdt: String?,
        override val isWrapped: Boolean,
        override var rate: BigDecimal?,
        override var currency: String = Constants.USD_READABLE_SYMBOL
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
        rate = rate,
        currency = currency
    )

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = mintAddress == WRAPPED_SOL_MINT

    @IgnoredOnParcel
    val isSpl: Boolean
        get() = mintAddress != WRAPPED_SOL_MINT

    @IgnoredOnParcel
    val isRenBTC: Boolean
        get() = tokenSymbol == REN_BTC_SYMBOL

    @IgnoredOnParcel
    val isUSDC: Boolean
        get() = tokenSymbol == USDC_SYMBOL

    @IgnoredOnParcel
    val isUSDT: Boolean
        get() = tokenSymbol == USDT_SYMBOL

    @IgnoredOnParcel
    val usdRateOrZero: BigDecimal
        get() = rate ?: BigDecimal.ZERO

    @IgnoredOnParcel
    val currencyFormattedRate: String
        get() = (rate ?: BigDecimal.ZERO).asCurrency(currencySymbol)

    @IgnoredOnParcel
    val currencySymbol: String
        get() = if (currency == Constants.USD_READABLE_SYMBOL) Constants.USD_SYMBOL else currency

    @IgnoredOnParcel
    val isActive: Boolean
        get() = this is Active

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
                totalInUsd = if (amount == 0L) null else exchangeRate?.let { total.multiply(it) },
                total = total.scaleLong(tokenData.decimals),
                rate = exchangeRate,
                visibility = TokenVisibility.DEFAULT,
                serumV3Usdc = tokenData.serumV3Usdc,
                serumV3Usdt = tokenData.serumV3Usdt,
                isWrapped = tokenData.isWrapped
            )
        }
    }
}

fun List<Token.Active>.findSolOrThrow(): Token.Active = first { it.isSOL }

fun List<Token.Active>.findSolOrNull(): Token.Active? = firstOrNull { it.isSOL }
