package com.p2p.wallet.dashboard.model.local

import android.os.Parcelable
import com.p2p.wallet.utils.roundCurrencyValue
import kotlinx.parcelize.Parcelize
import kotlin.math.pow

@Parcelize
data class Token(
    val tokenSymbol: String,
    val depositAddress: String,
    val decimals: Int,
    val mintAddress: String,
    val tokenName: String,
    val iconUrl: String,
    val price: Double,
    val amount: Double,
    val walletBinds: Double
) : Parcelable {

    // fixme: remove this after refactoring. We should avoid creating objects with default params
    constructor() : this(
        "",
        "",
        0,
        "",
        "",
        "",
        0.0,
        0.0,
        0.0
    )

    companion object {
        private const val ADDRESS_SYMBOL_COUNT = 10

        /* fixme: workaround about adding hardcode wallet, looks strange */
        fun getSOL(publicKey: String, amount: Long) = Token(
            tokenSymbol = "SOL",
            tokenName = "SOL",
            mintAddress = "SOLMINT",
            iconUrl = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/solana/info/logo.png",
            depositAddress = publicKey,
            decimals = 9,
            amount = amount.toDouble() / (10.0.pow(9)),
            price = 0.0,
            walletBinds = 0.0
        )
    }

    @Suppress("MagicNumber")
    fun getFormattedAddress(): String {
        if (depositAddress.length < ADDRESS_SYMBOL_COUNT) {
            return depositAddress
        }

        val firstSix = depositAddress.take(6)
        val lastFour = depositAddress.takeLast(4)
        return "$firstSix...$lastFour"
    }

    fun getFormattedPrice(): String = "${price.roundCurrencyValue()} $"

    fun getFormattedTotal(): String = "$amount $tokenSymbol"
}