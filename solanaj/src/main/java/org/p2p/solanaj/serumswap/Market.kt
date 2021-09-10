package org.p2p.solanaj.serumswap

import org.p2p.solanaj.model.core.PublicKey
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow

class Market(
    val programId: PublicKey,
    private val decoded: MarketStatLayout,
    private val baseSplTokenDecimals: Int,
    private val quoteSplTokenDecimals: Int,
    private val skipPreflight: Boolean,
    private val commitment: String,
    private val layoutOverride: MarketStatLayout.Type?
) {

    companion object {
        fun getLayoutType(programId: String): MarketStatLayout.Type {
            val version = Version.getVersion(programId)
            return if (version == 1) MarketStatLayout.Type.LAYOUT_V1
            else MarketStatLayout.Type.LAYOUT_V2
        }

        fun getLayoutSpan(programId: String): Int =
            getLayoutType(programId).span.toInt()
    }

    val address: PublicKey = decoded.ownAddress
    val publicKey: PublicKey = address
    val baseMintAddress: PublicKey = decoded.baseMint
    val quoteMintAddress: PublicKey = decoded.quoteMint
    val bidsAddress: PublicKey = decoded.bids
    val asksAddress: PublicKey = decoded.asks
    val eventQueue: PublicKey = decoded.eventQueue
    val requestQueue: PublicKey = decoded.requestQueue
    val coinVault: PublicKey = decoded.baseVault
    val pcVault: PublicKey = decoded.quoteVault

    private val baseSplTokenMultiplier: BigDecimal
        get() = BigDecimal(10.0.pow(baseSplTokenDecimals))

    private val quoteSplTokenMultiplier: BigDecimal
        get() = BigDecimal(10.0.pow(quoteSplTokenDecimals))

    fun priceLotsToNumber(price: BigInteger): BigDecimal =
        (BigDecimal(price).multiply(BigDecimal(decoded.quoteLotSize)).multiply(baseSplTokenMultiplier)) /
            BigDecimal(decoded.baseLotSize).multiply(quoteSplTokenMultiplier)

    fun baseSizeLotsToNumber(quantity: BigInteger): BigDecimal =
        (BigDecimal(quantity).multiply(BigDecimal(decoded.baseLotSize))) / baseSplTokenMultiplier
}