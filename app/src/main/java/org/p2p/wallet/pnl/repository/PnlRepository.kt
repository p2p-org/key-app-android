package org.p2p.wallet.pnl.repository

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.api.PnlDataTimeSpan
import org.p2p.wallet.pnl.models.PnlData

interface PnlRepository {
    suspend fun getPnlData(
        userWallet: Base58String,
        tokenMints: List<Base58String> = emptyList(),
        timeSpan: PnlDataTimeSpan = PnlDataTimeSpan.LAST_24_HOURS
    ): PnlData
}
