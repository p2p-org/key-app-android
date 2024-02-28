package org.p2p.wallet.pnl.interactor

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

sealed class PnlDataState {
    object Idle : PnlDataState()
    object Loading : PnlDataState()
    data class Loaded(val data: PnlData) : PnlDataState()
    data class Error(val error: Throwable) : PnlDataState()

    fun toLoadedOrNull(): PnlData? = (this as? Loaded)?.data
    fun isLoaded(): Boolean = this is Loaded

    fun findForToken(tokenMint: Base58String): PnlTokenData? = toLoadedOrNull()?.findForToken(tokenMint)
}
