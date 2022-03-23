package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigInteger

sealed class MarketStatLayout(
    open val accountFlags: AccountFlags,
    open val ownAddress: PublicKey,
    open val vaultSignerNonce: BigInteger,
    open val baseMint: PublicKey,
    open val quoteMint: PublicKey,
    open val baseVault: PublicKey,
    open val baseDepositsTotal: BigInteger,
    open val baseFeesAccrued: BigInteger,
    open val quoteVault: PublicKey,
    open val quoteDepositsTotal: BigInteger,
    open val quoteFeesAccrued: BigInteger,
    open val quoteDustThreshold: BigInteger,
    open val requestQueue: PublicKey,
    open val eventQueue: PublicKey,
    open val bids: PublicKey,
    open val asks: PublicKey,
    open val baseLotSize: BigInteger,
    open val quoteLotSize: BigInteger,
    open val feeRateBps: BigInteger,
    open val referrerRebatesAccrued: BigInteger?
) {

    data class LayoutV1(
        override val accountFlags: AccountFlags,
        override val ownAddress: PublicKey,
        override val vaultSignerNonce: BigInteger,
        override val baseMint: PublicKey,
        override val quoteMint: PublicKey,
        override val baseVault: PublicKey,
        override val baseDepositsTotal: BigInteger,
        override val baseFeesAccrued: BigInteger,
        override val quoteVault: PublicKey,
        override val quoteDepositsTotal: BigInteger,
        override val quoteFeesAccrued: BigInteger,
        override val quoteDustThreshold: BigInteger,
        override val requestQueue: PublicKey,
        override val eventQueue: PublicKey,
        override val bids: PublicKey,
        override val asks: PublicKey,
        override val baseLotSize: BigInteger,
        override val quoteLotSize: BigInteger,
        override val feeRateBps: BigInteger
    ) : MarketStatLayout(
        accountFlags = accountFlags,
        ownAddress = ownAddress,
        vaultSignerNonce = vaultSignerNonce,
        baseMint = baseMint,
        quoteMint = quoteMint,
        baseVault = baseVault,
        baseDepositsTotal = baseDepositsTotal,
        baseFeesAccrued = baseFeesAccrued,
        quoteVault = quoteVault,
        quoteDepositsTotal = quoteDepositsTotal,
        quoteFeesAccrued = quoteFeesAccrued,
        quoteDustThreshold = quoteDustThreshold,
        requestQueue = requestQueue,
        eventQueue = eventQueue,
        bids = bids,
        asks = asks,
        baseLotSize = baseLotSize,
        quoteLotSize = quoteLotSize,
        feeRateBps = feeRateBps,
        referrerRebatesAccrued = null
    )

    data class LayoutV2(
        override val accountFlags: AccountFlags,
        override val ownAddress: PublicKey,
        override val vaultSignerNonce: BigInteger,
        override val baseMint: PublicKey,
        override val quoteMint: PublicKey,
        override val baseVault: PublicKey,
        override val baseDepositsTotal: BigInteger,
        override val baseFeesAccrued: BigInteger,
        override val quoteVault: PublicKey,
        override val quoteDepositsTotal: BigInteger,
        override val quoteFeesAccrued: BigInteger,
        override val quoteDustThreshold: BigInteger,
        override val requestQueue: PublicKey,
        override val eventQueue: PublicKey,
        override val bids: PublicKey,
        override val asks: PublicKey,
        override val baseLotSize: BigInteger,
        override val quoteLotSize: BigInteger,
        override val feeRateBps: BigInteger,
        override val referrerRebatesAccrued: BigInteger
    ) : MarketStatLayout(
        accountFlags = accountFlags,
        ownAddress = ownAddress,
        vaultSignerNonce = vaultSignerNonce,
        baseMint = baseMint,
        quoteMint = quoteMint,
        baseVault = baseVault,
        baseDepositsTotal = baseDepositsTotal,
        baseFeesAccrued = baseFeesAccrued,
        quoteVault = quoteVault,
        quoteDepositsTotal = quoteDepositsTotal,
        quoteFeesAccrued = quoteFeesAccrued,
        quoteDustThreshold = quoteDustThreshold,
        requestQueue = requestQueue,
        eventQueue = eventQueue,
        bids = bids,
        asks = asks,
        baseLotSize = baseLotSize,
        quoteLotSize = quoteLotSize,
        feeRateBps = feeRateBps,
        referrerRebatesAccrued = referrerRebatesAccrued
    )

    enum class Type {
        LAYOUT_V1, LAYOUT_V2;
    }
}
