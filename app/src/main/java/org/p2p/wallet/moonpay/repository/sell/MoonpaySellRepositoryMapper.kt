package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionResponse
import org.p2p.wallet.utils.Base58String

class MoonpaySellRepositoryMapper {
    fun fromNetwork(
        response: List<MoonpaySellTransactionResponse>,
        transactionOwnerAddress: Base58String
    ): List<MoonpaySellTransaction> {
        return response.map { transactionResponse ->
            transactionResponse.run {
                MoonpaySellTransaction(
                    transactionId = transactionId,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    status = MoonpaySellTransaction.TransactionStatus.fromString(status),
                    accountId = accountId,
                    customerId = customerId,
                    bankAccountId = bankAccountId,
                    externalTransactionId = externalTransactionId,
                    externalCustomerId = externalTransactionId,
                    countryAbbreviation = countryAbbreviation,
                    stateAbbreviation = stateAbbreviation,
                    userAddress = transactionOwnerAddress
                )
            }
        }
    }

    fun fromNetworkError(error: Throwable): MoonpaySellError {
        // add more errors if needed
        return if (error is IllegalStateException) {
            MoonpaySellError.UnknownError(error)
        } else {
            MoonpaySellError.UnauthorizedRequest(error)
        }
    }
}
