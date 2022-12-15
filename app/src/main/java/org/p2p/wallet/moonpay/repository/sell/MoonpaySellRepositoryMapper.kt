package org.p2p.wallet.moonpay.repository.sell

import okio.IOException
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.moonpay.MoonpayErrorResponseType
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyAmounts
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellPaymentMethod
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellQuoteResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
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

    fun fromNetwork(
        response: MoonpaySellQuoteResponse
    ): MoonpaySellTokenQuote {
        val tokenDetails = response.tokenDetails.run {
            MoonpayCurrency.CryptoToken(
                tokenSymbol = currencySymbol,
                tokenName = currencyName,
                currencyId = currencyId,
                amounts = MoonpayCurrencyAmounts(
                    minAmount = minAmount.toBigDecimal(),
                    maxAmount = maxAmount.toBigDecimal(),
                    minBuyAmount = minBuyAmount.toBigDecimal(),
                    maxBuyAmount = maxBuyAmount.toBigDecimal(),
                    minSellAmount = minSellAmount.toBigDecimal(),
                    maxSellAmount = maxSellAmount.toBigDecimal()
                )
            )
        }
        val fiatDetails = response.fiatDetails.run {
            MoonpayCurrency.Fiat(
                fiatCode = currencySymbol,
                fiatName = currencyName,
                currencyId = currencyId,
                amounts = MoonpayCurrencyAmounts(
                    minAmount = minAmount.toBigDecimal(),
                    maxAmount = maxAmount.toBigDecimal(),
                    minBuyAmount = minBuyAmount.toBigDecimal(),
                    maxBuyAmount = maxBuyAmount.toBigDecimal(),
                    minSellAmount = minSellAmount.toBigDecimal(),
                    maxSellAmount = maxSellAmount.toBigDecimal()
                )
            )
        }
        return response.run {
            MoonpaySellTokenQuote(
                tokenDetails = tokenDetails,
                tokenAmount = tokenAmount,
                tokenPrice = tokenPrice,
                fiatDetails = fiatDetails,
                paymentMethod = MoonpaySellPaymentMethod.fromStringValue(paymentMethod),
                extraFeeAmount = extraFeeAmount,
                feeAmount = feeAmount,
                fiatEarning = fiatEarning
            )
        }
    }

    fun fromNetworkError(error: Throwable): MoonpaySellError {
        // add more errors if needed
        return when (error) {
            is ServerException -> {
                val moonpayErrorType = error.jsonErrorBody
                    ?.getAsJsonPrimitive("type")
                    ?.asString
                if (moonpayErrorType == MoonpayErrorResponseType.NOT_FOUND_ERROR.stringValue) {
                    MoonpaySellError.TokenToSellNotFound(error)
                } else {
                    MoonpaySellError.UnknownError(error)
                }
            }
            is IllegalStateException, is IOException -> {
                MoonpaySellError.UnknownError(error)
            }
            else -> {
                MoonpaySellError.UnauthorizedRequest(error)
            }
        }
    }
}
