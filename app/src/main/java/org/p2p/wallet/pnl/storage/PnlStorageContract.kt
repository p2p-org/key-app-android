package org.p2p.wallet.pnl.storage

import java.time.ZonedDateTime
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

interface PnlStorageContract {

    companion object {
        const val EXPIRE_TIME_MINUTES: Long = 5
    }

    var total: PnlTokenData
    var tokens: Map<Base58String, PnlTokenData>
    var lastUpdateTime: ZonedDateTime?

    fun hasToken(mintAddress: Base58String): Boolean = tokens.containsKey(mintAddress)

    fun hasAllTokens(mintAddresses: List<Base58String>): Boolean {
        return tokens.keys.containsAll(mintAddresses)
    }

    fun isExpired(): Boolean {
        return lastUpdateTime == null || lastUpdateTime!!
            .plusMinutes(EXPIRE_TIME_MINUTES)
            .isBefore(ZonedDateTime.now())
    }

    suspend fun getOrCache(tokenMints: List<Base58String>, dataGetter: suspend () -> PnlData): PnlData {
        if (isExpired()) {
            val data = dataGetter()
            total = data.total
            tokens += data.tokens
            lastUpdateTime = ZonedDateTime.now()
        }
        return PnlData(total, tokens)
    }
}
