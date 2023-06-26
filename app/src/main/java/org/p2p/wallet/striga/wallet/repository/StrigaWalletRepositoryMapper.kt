package org.p2p.wallet.striga.wallet.repository

import org.threeten.bp.ZonedDateTime
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toBigIntegerOrZero
import org.p2p.wallet.striga.wallet.api.response.StrigaBlockchainNetworkResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaEnrichFiatAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaInitiateOnchainWithdrawalResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressItemResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressesResponse
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountStatus
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitiateOnchainWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxStatus
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxType
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

class StrigaWalletRepositoryMapper {

    fun fromNetwork(response: StrigaEnrichFiatAccountResponse): StrigaFiatAccountDetails = with(response) {
        StrigaFiatAccountDetails(
            currency = currency,
            status = StrigaFiatAccountStatus.from(status),
            internalAccountId = internalAccountId,
            bankName = bankName,
            bankCountry = bankCountry,
            bankAddress = bankAddress,
            bankAccountHolderName = bankAccountHolderName,
            iban = iban,
            bic = bic,
            accountNumber = accountNumber,
            provider = provider,
            paymentType = paymentType,
            domestic = domestic,
            routingCodeEntries = routingCodeEntries,
            payInReference = payInReference,
        )
    }

    fun fromNetwork(response: StrigaWhitelistedAddressesResponse): List<StrigaWhitelistedAddressItem> {
        return response.addresses.map(this::fromNetwork)
    }

    fun fromNetwork(response: StrigaInitiateOnchainWithdrawalResponse): StrigaInitiateOnchainWithdrawalDetails {
        return StrigaInitiateOnchainWithdrawalDetails(
            challengeId = StrigaWithdrawalChallengeId(response.challengeId),
            dateExpires = ZonedDateTime.parse(response.dateExpires),
            transaction = response.transaction.toDetailsTransaction(),
            feeEstimate = response.feeEstimate.toDetailsFeeEstimate(),
        )
    }

    fun fromNetwork(item: StrigaWhitelistedAddressItemResponse): StrigaWhitelistedAddressItem {
        return StrigaWhitelistedAddressItem(
            id = StrigaWhitelistedAddressId(item.id),
            status = StrigaWhitelistedAddressItem.Status.valueOf(item.status),
            address = item.address,
            currency = StrigaNetworkCurrency.valueOf(item.currency),
            network = fromNetwork(item.network),
        )
    }

    fun fromNetwork(response: StrigaBlockchainNetworkResponse): StrigaBlockchainNetworkInfo {
        return StrigaBlockchainNetworkInfo(
            name = response.name,
            contractAddress = response.contractAddress,
            type = response.type,
        )
    }

    private fun StrigaInitiateOnchainWithdrawalResponse.FeeEstimate.toDetailsFeeEstimate():
        StrigaInitiateOnchainWithdrawalDetails.FeeEstimate {
        return StrigaInitiateOnchainWithdrawalDetails.FeeEstimate(
            totalFee = totalFee.toBigIntegerOrZero(),
            networkFee = networkFee.toBigIntegerOrZero(),
            ourFee = ourFee.toBigIntegerOrZero(),
            theirFee = theirFee.toBigIntegerOrZero(),
            feeCurrency = StrigaNetworkCurrency.valueOf(feeCurrency),
            gasLimit = gasLimit.toBigIntegerOrZero(),
            gasPrice = gasPrice.toBigDecimalOrZero(),
        )
    }

    private fun StrigaInitiateOnchainWithdrawalResponse.Transaction.toDetailsTransaction():
        StrigaInitiateOnchainWithdrawalDetails.Transaction {
        return StrigaInitiateOnchainWithdrawalDetails.Transaction(
            userId = syncedOwnerId,
            sourceAccountId = StrigaAccountId(sourceAccountId),
            parentWalletId = StrigaWalletId(parentWalletId),
            currency = StrigaNetworkCurrency.valueOf(currency),
            amount = amount.toBigIntegerOrZero(),
            status = StrigaOnchainTxStatus.from(status),
            txType = StrigaOnchainTxType.from(txType),
            blockchainDestinationAddress = blockchainDestinationAddress,
            blockchainNetwork = StrigaBlockchainNetworkInfo(
                name = blockchainNetwork.name,
                contractAddress = blockchainNetwork.contractAddress,
                type = blockchainNetwork.type,
            ),
            transactionCurrency = StrigaNetworkCurrency.valueOf(transactionCurrency),
        )
    }
}
