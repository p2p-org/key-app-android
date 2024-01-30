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

    companion object {
        val DEFAULT_TIME_SPAN = PnlDataTimeSpan.LAST_24_HOURS
    }

    suspend fun getPnlData(
        tokenMints: List<Base58String>,
        timeSpan: PnlDataTimeSpan = DEFAULT_TIME_SPAN,
    ): PnlData = pnlRepository.getPnlData(timeSpan, tokenKeyProvider.publicKeyBase58, tokenMints)

    suspend fun getPnlForToken(
        tokenMint: Base58String,
        timeSpan: PnlDataTimeSpan = DEFAULT_TIME_SPAN,
    ): PnlTokenData? {
        val pnlData = pnlRepository.getPnlData(timeSpan, tokenKeyProvider.publicKeyBase58, listOf(tokenMint))
        return pnlData.findToken(tokenMint)
    }
}
