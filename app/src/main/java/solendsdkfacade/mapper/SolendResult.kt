package solendsdkfacade.mapper

import solendsdkfacade.model.SolendMethodResultSuccess
import solendsdkfacade.model.SolendMethodResultError

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
