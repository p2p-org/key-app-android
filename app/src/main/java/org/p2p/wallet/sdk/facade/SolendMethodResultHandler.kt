package org.p2p.wallet.sdk.facade

import org.p2p.wallet.sdk.facade.model.SolendMethodResultError
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess

interface SolendMethodResultHandler {
    fun handleResultSuccess(result: SolendMethodResultSuccess)
    fun handleResultError(error: SolendMethodResultError)
}
