package solendsdkfacade.model

sealed interface SolendMethodResultSuccess {
    class SolendDepositTransactions(
        val transactions: List<String>
    ) : SolendMethodResultSuccess

    class SolendWithdrawTransactions(
        val transactions: List<String>
    ) : SolendMethodResultSuccess
}
