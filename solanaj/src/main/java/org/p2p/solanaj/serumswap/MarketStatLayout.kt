package org.p2p.solanaj.serumswap

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigInteger

private const val LAYOUT_V1_SPAN = 380L

open class MarketStatLayout(
    val data: ByteArray,
    val length: Int
) : AbstractData(data, length) {

    lateinit var accountFlags: AccountFlags
    lateinit var ownAddress: PublicKey
    lateinit var vaultSignerNonce: BigInteger
    lateinit var baseMint: PublicKey
    lateinit var quoteMint: PublicKey
    lateinit var baseVault: PublicKey
    lateinit var baseDepositsTotal: BigInteger
    lateinit var baseFeesAccrued: BigInteger
    lateinit var quoteVault: PublicKey
    lateinit var quoteDepositsTotal: BigInteger
    lateinit var quoteFeesAccrued: BigInteger
    lateinit var quoteDustThreshold: BigInteger
    lateinit var requestQueue: PublicKey
    lateinit var eventQueue: PublicKey
    lateinit var bids: PublicKey
    lateinit var asks: PublicKey
    lateinit var baseLotSize: BigInteger
    lateinit var quoteLotSize: BigInteger
    lateinit var feeRateBps: BigInteger

    fun initializeCustom(
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

    fun initialize() {
        // skipping five bytes
        skipBytes(5)
        accountFlags = AccountFlags(readUint64())
        ownAddress = readPublicKey()
        vaultSignerNonce = readUint64()
        baseMint = readPublicKey()
        quoteMint = readPublicKey()
        baseVault = readPublicKey()
        baseDepositsTotal = readUint64()
        baseFeesAccrued = readUint64()
        quoteVault = readPublicKey()
        quoteDepositsTotal = readUint64()
        quoteFeesAccrued = readUint64()
        quoteDustThreshold = readUint64()
        requestQueue = readPublicKey()
        eventQueue = readPublicKey()
        bids = readPublicKey()
        asks = readPublicKey()
        baseLotSize = readUint64()
        quoteLotSize = readUint64()
        feeRateBps = readUint64()
    }

    enum class Type(val span: Long) {
        LAYOUT_V1(LAYOUT_V1_SPAN),
        LAYOUT_V2(LAYOUT_V1_SPAN + 8);
    }

    class LayoutV1 constructor(data: ByteArray) : MarketStatLayout(data, LAYOUT_V1_SPAN.toInt()) {

        init {
            initialize()
        }
    }

    class LayoutV2 constructor(data: ByteArray) : MarketStatLayout(data, LAYOUT_V1_SPAN.toInt()) {
        lateinit var referrerRebatesAccrued: BigInteger

        init {
            initialize()
            referrerRebatesAccrued = readUint64()
        }
    }
}