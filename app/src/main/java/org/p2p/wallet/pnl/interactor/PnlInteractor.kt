package org.p2p.wallet.pnl.interactor

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.pnl.api.PnlDataTimeSpan
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData
import org.p2p.wallet.pnl.repository.PnlRepository

class PnlInteractor(
    private val pnlRepository: PnlRepository,
    private val tokenKeyProvider: TokenKeyProvider,
) {

    suspend fun getPnlData(
        tokenMints: List<Base58String>,
        timeSpan: PnlDataTimeSpan = PnlDataTimeSpan.LAST_24_HOURS,
    ): PnlData = pnlRepository.getPnlData(tokenKeyProvider.publicKeyBase58, tokenMints, timeSpan)

    suspend fun getPnlForToken(
        tokenMint: Base58String,
        timeSpan: PnlDataTimeSpan = PnlDataTimeSpan.LAST_24_HOURS,
    ): PnlTokenData? {
        val pnlData = pnlRepository.getPnlData(tokenKeyProvider.publicKeyBase58, listOf(tokenMint), timeSpan)
        return pnlData.findForToken(tokenMint)
    }
}
