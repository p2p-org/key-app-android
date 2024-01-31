package org.p2p.wallet.pnl.storage

import java.time.ZonedDateTime
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

interface PnlStorageContract {
    fun getTotalPnlData(): PnlTokenData
    fun setTotalPnlData(pnlTokenData: PnlTokenData)
    fun getTokensPnlData(): Map<Base58String, PnlTokenData>
    fun addTokensPnlData(data: Map<Base58String, PnlTokenData>)
    fun getLastUpdatedTime(): ZonedDateTime?
    fun setLastUpdatedTime(time: ZonedDateTime)

    fun hasToken(mintAddress: Base58String): Boolean
    fun hasAllTokens(mintAddresses: List<Base58String>): Boolean

    fun isCacheExpired(): Boolean
    suspend fun getOrCache(dataGetter: suspend () -> PnlData): PnlData
}
