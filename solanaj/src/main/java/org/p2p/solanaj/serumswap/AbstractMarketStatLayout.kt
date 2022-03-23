package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigInteger

abstract class AbstractMarketStatLayout {

    abstract var accountFlags: AccountFlags
    abstract var ownAddress: PublicKey
    abstract var vaultSignerNonce: BigInteger
    abstract var baseMint: PublicKey
    abstract var quoteMint: PublicKey
    abstract var baseVault: PublicKey
    abstract var baseDepositsTotal: BigInteger
    abstract var baseFeesAccrued: BigInteger
    abstract var quoteVault: PublicKey
    abstract var quoteDepositsTotal: BigInteger
    abstract var quoteFeesAccrued: BigInteger
    abstract var quoteDustThreshold: BigInteger
    abstract var requestQueue: PublicKey
    abstract var eventQueue: PublicKey
    abstract var bids: PublicKey
    abstract var asks: PublicKey
    abstract var baseLotSize: BigInteger
    abstract var quoteLotSize: BigInteger
    abstract var feeRateBps: BigInteger

    fun initialize(
        accountFlags: AccountFlags,
        ownAddress: PublicKey,
        vaultSignerNonce: BigInteger,
        baseMint: PublicKey,
        quoteMint: PublicKey,
        baseVault: PublicKey,
        baseDepositsTotal: BigInteger,
        baseFeesAccrued: BigInteger,
        quoteVault: PublicKey,
        quoteDepositsTotal: BigInteger,
        quoteFeesAccrued: BigInteger,
        quoteDustThreshold: BigInteger,
        requestQueue: PublicKey,
        eventQueue: PublicKey,
        bids: PublicKey,
        asks: PublicKey,
        baseLotSize: BigInteger,
        quoteLotSize: BigInteger,
        feeRateBps: BigInteger
    ) {

        this.accountFlags = accountFlags
        this.ownAddress = ownAddress
        this.vaultSignerNonce = vaultSignerNonce
        this.baseMint = baseMint
        this.quoteMint = quoteMint
        this.baseVault = baseVault
        this.baseDepositsTotal = baseDepositsTotal
        this.baseFeesAccrued = baseFeesAccrued
        this.quoteVault = quoteVault
        this.quoteDepositsTotal = quoteDepositsTotal
        this.quoteFeesAccrued = quoteFeesAccrued
        this.quoteDustThreshold = quoteDustThreshold
        this.requestQueue = requestQueue
        this.eventQueue = eventQueue
        this.bids = bids
        this.asks = asks
        this.baseLotSize = baseLotSize
        this.quoteLotSize = quoteLotSize
        this.feeRateBps = feeRateBps
    }
}
