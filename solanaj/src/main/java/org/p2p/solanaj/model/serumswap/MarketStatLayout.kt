package org.p2p.solanaj.model.serumswap

import org.p2p.solanaj.model.core.PublicKey

open class MarketStatLayout(
    open val span: Long,
    open val accountFlags: AccountFlags,
    open val ownAddress: PublicKey,
    open val vaultSignerNonce: Long,
    open val baseMint: PublicKey,
    open val quoteMint: PublicKey,
    open val baseVault: PublicKey,
    open val baseDepositsTotal: Long,
    open val baseFeesAccrued: Long,
    open val quoteVault: PublicKey,
    open val quoteDepositsTotal: Long,
    open val quoteFeesAccrued: Long,
    open val quoteDustThreshold: Long,
    open val requestQueue: PublicKey,
    open val eventQueue: PublicKey,
    open val bids: PublicKey,
    open val asks: PublicKey,
    open val baseLotSize: Long,
    open val quoteLotSize: Long,
    open val feeRateBps: Long
)