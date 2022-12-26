package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyAmounts
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellPaymentMethod
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellQuoteResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTransactionDepositWalletResponse
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.model.SellTransactionAmounts
import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionShortResponse
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.utils.Base58String

class MoonpaySellRepositoryMapper {
    fun fromNetwork(
        response: List<MoonpaySellTransactionShortResponse>,
        depositWallets: List<MoonpaySellTransactionDepositWalletResponse>,
        selectedFiat: SellTransactionFiatCurrency,
        transactionOwnerAddress: Base58String,
    ): List<SellTransaction> = response.mapNotNull { transaction ->
        val amounts = transaction.createAmounts()
        val metadata = transaction.createMetadata()

        when (transaction.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                val moonpayDepositWalletAddress =
                    depositWallets.firstOrNull { it.transactionId == transaction.transactionId }
                        ?.depositWallet
                        ?.walletAddress
                        ?: return@mapNotNull null // skip those that we can't fetch

                SellTransaction.WaitingForDepositTransaction(
                    metadata = metadata,
                    transactionId = transaction.transactionId,
                    amounts = amounts,
                    userAddress = transactionOwnerAddress,
                    selectedFiat = selectedFiat,
                    moonpayDepositWalletAddress = moonpayDepositWalletAddress
                )
            }
            SellTransactionStatus.PENDING -> {
                SellTransaction.PendingTransaction(
                    metadata = metadata,
                    transactionId = transaction.transactionId,
                    amounts = amounts,
                    selectedFiat = selectedFiat,
                    userAddress = transactionOwnerAddress,
                )
            }
            SellTransactionStatus.COMPLETED -> {
                SellTransaction.CompletedTransaction(
                    metadata = metadata,
                    transactionId = transaction.transactionId,
                    amounts = amounts,
                    selectedFiat = selectedFiat,
                    userAddress = transactionOwnerAddress,
                )
            }
            SellTransactionStatus.FAILED -> {
                SellTransaction.FailedTransaction(
                    metadata = metadata,
                    transactionId = transaction.transactionId,
                    amounts = amounts,
                    selectedFiat = selectedFiat,
                    userAddress = transactionOwnerAddress,
                )
            }
        }
    }

    private fun MoonpaySellTransactionShortResponse.createAmounts(): SellTransactionAmounts {
        return SellTransactionAmounts(
            tokenAmount = tokenAmount.toBigDecimal(),
            feeAmount = (feeAmount ?: 0.0).toBigDecimal(),
            usdAmount = usdRate.toBigDecimal(),
            eurAmount = eurRate.toBigDecimal(),
            gbpAmount = gbpRate.toBigDecimal()
        )
    }

    private fun MoonpaySellTransactionShortResponse.createMetadata(): SellTransaction.SellTransactionMetadata {
        return SellTransaction.SellTransactionMetadata(
            createdAt = createdAt,
            updatedAt = updatedAt,
            accountId = accountId,
            customerId = customerId,
            bankAccountId = bankAccountId,
            externalTransactionId = externalTransactionId,
            externalCustomerId = externalCustomerId,
            countryAbbreviation = countryAbbreviation,
            stateAbbreviation = stateAbbreviation
        )
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
                tokenAmount = tokenAmount.toBigDecimal(),
                tokenPrice = tokenPrice.toBigDecimal(),
                fiatDetails = fiatDetails,
                paymentMethod = MoonpaySellPaymentMethod.fromStringValue(paymentMethod),
                extraFeeAmount = extraFeeAmount,
                feeAmount = feeAmount.toBigDecimal(),
                fiatEarning = fiatEarning.toBigDecimal()
            )
        }
    }
}
