package org.p2p.wallet.sdk.facade.mapper

import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess
import org.p2p.wallet.sdk.facade.model.SolendMethodResultError

class SolendResult<SuccessType : SolendMethodResultSuccess>(
    private val success: SuccessType?,
    private val error: SolendMethodResultError?
) {
    fun onResultError(block: (error: SolendMethodResultError) -> Unit): SolendResult<SuccessType> = apply {
        error?.let(block)
    }

    fun onResultSuccess(block: (result: SuccessType) -> Unit): SolendResult<SuccessType> = apply {
        success?.let(block)
    }
}
