package org.p2p.wallet.striga.wallet.repository.mapper

import timber.log.Timber
import org.p2p.wallet.striga.StrigaUserConstants.USER_FILTER_START_DATE
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.wallet.api.request.StrigaAccountStatementRequest
import org.p2p.wallet.striga.wallet.api.response.StrigaAccountStatementResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaAccountStatementResponse.AccountTransactionResponse
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId

class StrigaAccountStatementMapper(
    private val userIdProvider: StrigaUserIdProvider
) {
    private companion object {
        private const val OFF_RAMP_COMPLETED_TX_TYPE = "SEPA_PAYOUT_COMPLETED"
        private const val OFF_RAMP_INITIATED_TX_TYPE = "SEPA_PAYOUT_INITIATED"
        private const val ON_RAMP_COMPLETED_TX_TYPE = "SEPA_PAYIN_COMPLETED"
    }

    fun toNetworkAccountStatement(accountId: StrigaAccountId): StrigaAccountStatementRequest {
        return StrigaAccountStatementRequest(
            userId = userIdProvider.getUserIdOrThrow(),
            accountId = accountId.value,
            startDate = USER_FILTER_START_DATE,
            endDate = System.currentTimeMillis(),
        )
    }

    /**
     * Get the banking details (bic / iban) from the:
     * 1) initial off-ramp transaction
     * 2) completed on-ramp transaction
     * @throws IllegalArgumentException if there is not enough data
     */
    fun fromNetwork(
        response: StrigaAccountStatementResponse,
        strigaUserFullName: String
    ): StrigaUserBankingDetails {
        val transactions = response.transactions

        val transactionWithUserBankingDetails =
            getInitialOffRampTransaction(transactions) ?: getCompletedOnRampTransaction(transactions)

        if (transactionWithUserBankingDetails == null) {
            Timber.i("Not enough data for banking details: no transaction with full details; total=${response.total}")
            return StrigaUserBankingDetails(
                bankingBic = null,
                bankingIban = null,
                bankingFullName = strigaUserFullName
            )
        }

        return transactionWithUserBankingDetails.extractBankingDetails(strigaUserFullName)
    }

    private fun getInitialOffRampTransaction(
        transactions: List<AccountTransactionResponse>
    ): AccountTransactionResponse? {
        val completedOffRampTransactionId = transactions.firstOrNull { it.txType == OFF_RAMP_COMPLETED_TX_TYPE }?.id
        return transactions.firstOrNull {
            it.id == completedOffRampTransactionId && it.txType == OFF_RAMP_INITIATED_TX_TYPE
        }
    }

    private fun getCompletedOnRampTransaction(
        transactions: List<AccountTransactionResponse>
    ): AccountTransactionResponse? {
        return transactions.firstOrNull { it.txType == ON_RAMP_COMPLETED_TX_TYPE }
    }

    private fun AccountTransactionResponse.extractBankingDetails(
        strigaUserFullName: String
    ): StrigaUserBankingDetails {
        requireNotNull(bankingSenderBic) { "No sender bic found" }
        requireNotNull(bankingSenderIban) { "No sender iban found" }

        return StrigaUserBankingDetails(
            bankingBic = bankingSenderBic,
            bankingIban = bankingSenderIban,
            bankingFullName = strigaUserFullName
        )
    }
}
