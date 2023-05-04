package org.p2p.wallet.bridge.api.mapper

import org.threeten.bp.ZonedDateTime
import org.p2p.core.utils.Constants
import org.p2p.core.utils.orZero
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.bridge.api.response.BridgeAmountResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse
import org.p2p.wallet.bridge.api.response.BridgeSendFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeSendStatusResponse
import org.p2p.wallet.bridge.api.response.BridgeSendTransactionResponse
import org.p2p.wallet.bridge.api.response.BridgeTransactionStatusResponse
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionStatus
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.bridge.BridgeHistoryTransaction

class BridgeMapper {

    fun fromNetwork(response: BridgeBundleResponse): BridgeBundle {
        return BridgeBundle(
            bundleId = response.bundleId,
            userWallet = response.userWallet,
            recipient = response.recipient,
            resultAmount = fromNetwork(response.resultAmount),
            token = response.erc20TokenAddress,
            expiresAt = response.expiresAt ?: System.currentTimeMillis(),
            transactions = response.transactions.orEmpty(),
            signatures = response.signatures.orEmpty(),
            fees = fromNetwork(response.fees),
            status = response.status,
            claimKey = response.claimKey,
            compensationDeclineReason = response.compensationDeclineReason.orEmpty()
        )
    }

    fun fromNetwork(response: BridgeBundleFeesResponse?): BridgeBundleFees {
        return BridgeBundleFees(
            gasFee = fromNetwork(response?.gasFee),
            gasFeeInToken = fromNetwork(response?.gasFeeInToken),
            arbiterFee = fromNetwork(response?.arbiterFee),
            createAccount = fromNetwork(response?.createAccountFee)
        )
    }

    fun fromNetwork(response: BridgeAmountResponse?): BridgeFee {
        return BridgeFee(
            amount = response?.amount,
            amountInUsd = response?.usdAmount,
            chain = response?.chain,
            token = response?.tokenAddress,
            symbol = response?.symbol.orEmpty(),
            name = response?.tokenName.orEmpty(),
            decimals = response?.decimals.orZero()
        )
    }

    fun fromNetwork(response: BridgeSendFeesResponse): BridgeSendFees {
        return BridgeSendFees(
            networkFee = fromNetwork(response.networkFee),
            networkFeeInToken = fromNetwork(response.networkFeeInToken),
            messageAccountRent = fromNetwork(response.messageAccountRent),
            messageAccountRentInToken = fromNetwork(response.messageAccountRentInToken),
            bridgeFee = fromNetwork(response.bridgeFee),
            bridgeFeeInToken = fromNetwork(response.bridgeFeeInToken),
            arbiterFee = fromNetwork(response.arbiterFee),
            resultAmount = fromNetwork(response.resultAmount),
        )
    }

    fun fromNetwork(response: BridgeSendStatusResponse): BridgeSendTransactionStatus {
        return when (response) {
            BridgeSendStatusResponse.PENDING -> BridgeSendTransactionStatus.PENDING
            BridgeSendStatusResponse.FAILED -> BridgeSendTransactionStatus.FAILED
            BridgeSendStatusResponse.IN_PROGRESS -> BridgeSendTransactionStatus.IN_PROGRESS
            BridgeSendStatusResponse.COMPLETED -> BridgeSendTransactionStatus.COMPLETED
        }
    }

    fun fromNetwork(response: BridgeTransactionStatusResponse): BridgeSendTransactionDetails {
        return BridgeSendTransactionDetails(
            id = response.id,
            userWallet = response.userWallet,
            recipient = response.recipient,
            amount = response.amount.orZero(),
            fees = fromNetwork(response.fees),
            status = fromNetwork(response.status)
        )
    }

    fun fromNetwork(response: BridgeSendTransactionResponse): BridgeSendTransaction {
        return BridgeSendTransaction(
            transaction = response.transaction.toBase64Instance(),
            message = response.message
        )
    }

    fun toHistoryItem(claimBundle: BridgeBundle, mintAddress: String): HistoryTransaction? {
        if (claimBundle.recipient.raw == mintAddress || mintAddress == Constants.WRAPPED_SOL_MINT) {
            return BridgeHistoryTransaction.Claim(
                bundleId = claimBundle.bundleId,
                date = ZonedDateTime.now(),
                bundle = claimBundle
            )
        }
        return null
    }

    fun toHistoryItem(sendDetails: BridgeSendTransactionDetails, mintAddress: String): HistoryTransaction? {
        if (sendDetails.recipient.raw == mintAddress || mintAddress == Constants.WRAPPED_SOL_MINT) {
            return BridgeHistoryTransaction.Send(
                id = sendDetails.id,
                date = ZonedDateTime.now(),
                sendDetails = sendDetails
            )
        }
        return null
    }
}
