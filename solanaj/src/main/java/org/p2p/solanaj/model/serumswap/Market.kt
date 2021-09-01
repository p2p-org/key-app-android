package org.p2p.solanaj.model.serumswap

import org.bouncycastle.crypto.Commitment
import org.p2p.solanaj.model.core.PublicKey
import java.math.BigInteger
import kotlin.math.pow

class Market(
    private val decoded: MarketStatLayout,
    private val baseSplTokenDecimals: Int,
    private val quoteSplTokenDecimals: Int,
    private val skipPreflight: Boolean,
    private val commitment: Commitment,
    val programId: PublicKey,
    private val layoutOverride: SerumSwapMarketStatLayout.Type?,
    private val openOrdersAccountsCache: OpenOrdersAccountsCache,
    private val feeDiscountKeysCache: FeeDiscountKeysCache
) {

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

    private val baseSplTokenMultiplier
        get() = 10.0.pow(baseSplTokenDecimals)

    private val quoteSplTokenMultiplier
        get() = 10.0.pow(quoteSplTokenDecimals)

    class LayoutV1(
        override val span: Long,
        override val accountFlags: AccountFlags,
        override val ownAddress: PublicKey,
        override val vaultSignerNonce: Long,
        override val baseMint: PublicKey,
        override val quoteMint: PublicKey,
        override val baseVault: PublicKey,
        override val baseDepositsTotal: Long,
        override val baseFeesAccrued: Long,
        override val quoteVault: PublicKey,
        override val quoteDepositsTotal: Long,
        override val quoteFeesAccrued: Long,
        override val quoteDustThreshold: Long,
        override val requestQueue: PublicKey,
        override val eventQueue: PublicKey,
        override val bids: PublicKey,
        override val asks: PublicKey,
        override val baseLotSize: Long,
        override val quoteLotSize: Long,
        override val feeRateBps: Long
    ) : MarketStatLayout(
        span,
        accountFlags,
        ownAddress,
        vaultSignerNonce,
        baseMint,
        quoteMint,
        baseVault,
        baseDepositsTotal,
        baseFeesAccrued,
        quoteVault,
        quoteDepositsTotal,
        quoteFeesAccrued,
        quoteDustThreshold,
        requestQueue,
        eventQueue,
        bids,
        asks,
        baseLotSize,
        quoteLotSize,
        feeRateBps
    )

    class LayoutV2(
        override val span: Long,
        override val accountFlags: AccountFlags,
        override val ownAddress: PublicKey,
        override val vaultSignerNonce: Long,
        override val baseMint: PublicKey,
        override val quoteMint: PublicKey,
        override val baseVault: PublicKey,
        override val baseDepositsTotal: Long,
        override val baseFeesAccrued: Long,
        override val quoteVault: PublicKey,
        override val quoteDepositsTotal: Long,
        override val quoteFeesAccrued: Long,
        override val quoteDustThreshold: Long,
        override val requestQueue: PublicKey,
        override val eventQueue: PublicKey,
        override val bids: PublicKey,
        override val asks: PublicKey,
        override val baseLotSize: Long,
        override val quoteLotSize: Long,
        override val feeRateBps: Long
    ) : MarketStatLayout(
        span,
        accountFlags,
        ownAddress,
        vaultSignerNonce,
        baseMint,
        quoteMint,
        baseVault,
        baseDepositsTotal,
        baseFeesAccrued,
        quoteVault,
        quoteDepositsTotal,
        quoteFeesAccrued,
        quoteDustThreshold,
        requestQueue,
        eventQueue,
        bids,
        asks,
        baseLotSize,
        quoteLotSize,
        feeRateBps
    )

    data class FeeDiscountAccount(
        val balance: BigInteger,
        val mint: PublicKey,
        val pubkey: PublicKey,
        val feeTier: BigInteger
    )
}