package org.p2p.core.token

import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
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
import org.p2p.core.utils.orZero
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
    open val programId: String? = null,
    open val tokenName: String,
    open val iconUrl: String?,
    open val isWrapped: Boolean,
    open var rate: BigDecimal?,
    open var currency: String = Constants.USD_READABLE_SYMBOL,
    open val tokenExtensions: TokenExtensions,
) : Parcelable {

    @Parcelize
    data class Active constructor(
        // put tokenSymbol first for toString()
        override val tokenSymbol: String,
        override val publicKey: String,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val visibility: TokenVisibility,
        val tokenServiceAddress: String,
        override val tokenExtensions: TokenExtensions,
        override val decimals: Int,
        override val mintAddress: String,
        override val programId: String?,
        override val tokenName: String,
        override val iconUrl: String?,
        override val isWrapped: Boolean,
        override var rate: BigDecimal?,
        override var currency: String = Constants.USD_READABLE_SYMBOL,
    ) : Token(
        publicKey = publicKey,
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        programId = programId,
        tokenName = tokenName,
        iconUrl = iconUrl,
        isWrapped = isWrapped,
        rate = rate,
        currency = currency,
        tokenExtensions = tokenExtensions,
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

        @IgnoredOnParcel
        val isHidden: Boolean
            get() = visibility == TokenVisibility.HIDDEN

        @IgnoredOnParcel
        val canTokenBeHidden: Boolean
            get() = tokenExtensions.canTokenBeHidden != false

        fun isDefinitelyHidden(isZerosHidden: Boolean): Boolean {
            val isHiddenByUser = visibility == TokenVisibility.HIDDEN
            val isHiddenByDefault = isZerosHidden &&
                isZero &&
                visibility == TokenVisibility.DEFAULT
            val isHidden = isHiddenByUser || isHiddenByDefault
            return canTokenBeHidden && isHidden
        }

        fun getFormattedUsdTotal(includeSymbol: Boolean = true): String? {
            return if (includeSymbol) totalInUsd?.asUsd() else totalInUsd?.formatFiat()
        }

        fun getFormattedTotal(includeSymbol: Boolean = false): String =
            if (includeSymbol) {
                "${total.formatToken(decimals)} $tokenSymbol"
            } else {
                total.formatToken(decimals)
            }
    }

    @Parcelize
    data class Eth constructor(
        override val publicKey: String,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        var isClaiming: Boolean = false,
        var latestActiveBundleId: String? = null,
        val tokenServiceAddress: String = publicKey,
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
        isWrapped = false,
        rate = rate,
        currency = currency,
        tokenExtensions = TokenExtensions.NONE
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

        fun getFormattedTotal(includeSymbol: Boolean = false): String {
            val decimals = if (isEth && decimals > 8) 8 else decimals
            val amount = total.formatToken(decimals)
            return if (includeSymbol) {
                "$amount $tokenSymbol"
            } else {
                amount
            }
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
        override val isWrapped: Boolean,
        override var rate: BigDecimal?,
        override var currency: String = Constants.USD_READABLE_SYMBOL,
        override val tokenExtensions: TokenExtensions,
    ) : Token(
        publicKey = null,
        tokenSymbol = tokenSymbol,
        decimals = decimals,
        mintAddress = mintAddress,
        tokenName = tokenName,
        iconUrl = iconUrl,
        isWrapped = isWrapped,
        rate = rate,
        currency = currency,
        tokenExtensions = tokenExtensions
    )

    @IgnoredOnParcel
    val isSOL: Boolean
        get() = mintAddress == WRAPPED_SOL_MINT

    @IgnoredOnParcel
    val isSpl: Boolean
        get() = mintAddress != WRAPPED_SOL_MINT

    @IgnoredOnParcel
    val isToken2022: Boolean
        get() = programId == Constants.SOLANA_TOKEN_2022_PROGRAM_ID

    @IgnoredOnParcel
    val isRenBTC: Boolean
        get() = tokenSymbol == REN_BTC_SYMBOL

    @IgnoredOnParcel
    val isUSDC: Boolean
        get() = tokenSymbol.equals(USDC_SYMBOL, ignoreCase = true)

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
        get() = when (currency) {
            Constants.USD_READABLE_SYMBOL -> Constants.USD_SYMBOL
            Constants.GBP_READABLE_SYMBOL -> Constants.GBP_SYMBOL
            Constants.EUR_READABLE_SYMBOL -> Constants.EUR_SYMBOL
            else -> currency
        }

    @IgnoredOnParcel
    val isActive: Boolean
        get() = this is Active

    fun getFormattedName(): String = if (isSOL) SOL_NAME else tokenName

    companion object {
        fun createSOL(
            publicKey: String,
            tokenMetadata: TokenMetadata,
            amount: Long,
            solPrice: BigDecimal?
        ): Active {
            val total: BigDecimal = BigDecimal(amount).divide(tokenMetadata.decimals.toPowerValue())
            return Active(
                publicKey = publicKey,
                tokenSymbol = tokenMetadata.symbol,
                decimals = tokenMetadata.decimals,
                mintAddress = tokenMetadata.mintAddress,
                programId = null,
                tokenName = SOL_NAME,
                iconUrl = tokenMetadata.iconUrl,
                totalInUsd = if (amount == 0L) null else solPrice?.let { total.multiply(it) },
                total = total.scaleLong(tokenMetadata.decimals),
                rate = solPrice,
                tokenServiceAddress = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN,
                visibility = TokenVisibility.DEFAULT,
                isWrapped = tokenMetadata.isWrapped,
                tokenExtensions = TokenExtensions()
            )
        }
    }
}

fun List<Token.Active>.findSolOrThrow(): Token.Active = first { it.isSOL }

fun List<Token.Active>.findSolOrNull(): Token.Active? = firstOrNull { it.isSOL }

fun List<Token.Active>.findByMintAddress(mintAddress: String): Token.Active? =
    firstOrNull { it.mintAddress == mintAddress }

@JvmName("findByNullableMintAddress")
fun List<Token.Active>.findByMintAddress(mintAddress: String?): Token.Active? =
    mintAddress?.let(::findByMintAddress)

fun List<Token.Active>.sortedWithPreferredStableCoins(): List<Token.Active> {
    return sortedWith(
        compareBy<Token.Active> {
            when (it.tokenSymbol) {
                USDC_SYMBOL -> 1
                USDT_SYMBOL -> 2
                else -> 3
            }
        }
            .thenComparing(
                compareByDescending {
                    it.totalInUsd.orZero()
                }
            )
    )
}
