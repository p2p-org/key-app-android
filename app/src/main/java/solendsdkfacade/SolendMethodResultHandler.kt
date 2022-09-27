package solendsdkfacade

import solendsdkfacade.model.SolendMethodResultError
import solendsdkfacade.model.SolendMethodResultSuccess

interface SolendMethodResultHandler {
    fun handleResultSuccess(result: SolendMethodResultSuccess)
    fun handleResultError(error: SolendMethodResultError)
}
