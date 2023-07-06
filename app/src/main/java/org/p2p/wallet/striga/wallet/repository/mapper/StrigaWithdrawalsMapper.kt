package org.p2p.wallet.striga.wallet.repository.mapper

import org.threeten.bp.ZonedDateTime
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toBigIntegerOrZero
import org.p2p.wallet.striga.wallet.api.response.StrigaInitWithdrawalResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaOnchainWithdrawalFeeResponse
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxStatus
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxType
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

class StrigaWithdrawalsMapper {
    fun fromNetwork(response: StrigaInitWithdrawalResponse): StrigaInitWithdrawalDetails {
        return StrigaInitWithdrawalDetails(
            challengeId = StrigaWithdrawalChallengeId(response.challengeId),
            dateExpires = ZonedDateTime.parse(response.dateExpires),
            transaction = response.transaction.toDetailsTransaction(),
            feeEstimate = response.feeEstimate.toDetailsFeeEstimate(),
        )
    }

    fun fromNetwork(response: StrigaOnchainWithdrawalFeeResponse): StrigaOnchainWithdrawalFees {
        return response.toDetailsFeeEstimate()
    }

    private fun StrigaOnchainWithdrawalFeeResponse.toDetailsFeeEstimate(): StrigaOnchainWithdrawalFees {
        return StrigaOnchainWithdrawalFees(
            totalFee = totalFee.toBigIntegerOrZero(),
            networkFee = networkFee.toBigIntegerOrZero(),
            ourFee = ourFee.toBigIntegerOrZero(),
            theirFee = theirFee.toBigIntegerOrZero(),
            feeCurrency = StrigaNetworkCurrency.valueOf(feeCurrency),
            gasLimit = gasLimit.toBigIntegerOrZero(),
            gasPrice = gasPrice.toBigDecimalOrZero(),
        )
    }

    private fun StrigaInitWithdrawalResponse.WithdrawalTransactionResponse.toDetailsTransaction():
        StrigaInitWithdrawalDetails.WithdrawalTransactionDetails {
        return StrigaInitWithdrawalDetails.WithdrawalTransactionDetails(
            userId = syncedOwnerId,
            sourceAccountId = StrigaAccountId(sourceAccountId),
            parentWalletId = StrigaWalletId(parentWalletId),
            currency = StrigaNetworkCurrency.valueOf(currency),
            amountInUnits = amountInUnits.toBigIntegerOrZero(),
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
