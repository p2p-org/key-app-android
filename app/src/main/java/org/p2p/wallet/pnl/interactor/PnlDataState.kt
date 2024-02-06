package org.p2p.wallet.pnl.interactor

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

sealed class PnlDataState {
    object Idle : PnlDataState()
    object Loading : PnlDataState()
    data class Result(val data: PnlData) : PnlDataState()
    data class Error(val error: Throwable) : PnlDataState()

    fun toResultOrNull(): PnlData? = (this as? Result)?.data
    fun isResult(): Boolean = this is Result

    fun findForToken(tokenMint: Base58String): PnlTokenData? = toResultOrNull()?.findForToken(tokenMint)
}
