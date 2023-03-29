package org.p2p.wallet.bridge.api.mapper

import org.p2p.solanaj.utils.crypto.toBase64String
import org.p2p.wallet.bridge.api.response.BridgeAmountResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse
import org.p2p.wallet.bridge.api.response.BridgeSendFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeSendStatusResponse
import org.p2p.wallet.bridge.api.response.BridgeSendTransactionResponse
import org.p2p.wallet.bridge.api.response.BridgeTransactionStatusResponse
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionStatus
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

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
            compensationDeclineReason = response.compensationDeclineReason.orEmpty()
        )
    }

    fun fromNetwork(response: BridgeBundleFeesResponse?): BridgeBundleFees {
        return BridgeBundleFees(
            gasEth = fromNetwork(response?.gasFee),
            arbiterFee = fromNetwork(response?.arbiterFee),
            createAccount = fromNetwork(response?.createAccountFee)
        )
    }

    fun fromNetwork(response: BridgeAmountResponse?): BridgeFee {
        return BridgeFee(
            amount = response?.amount,
            amountInUsd = response?.usdAmount,
            chain = response?.chain,
            token = response?.tokenName
        )
    }

    fun fromNetwork(response: BridgeSendFeesResponse): BridgeSendFees {
        return BridgeSendFees(
            networkFee = fromNetwork(response.networkFee),
            messageAccountRent = fromNetwork(response.messageAccountRent),
            bridgeFee = fromNetwork(response.bridgeFee),
            arbiterFee = fromNetwork(response.arbiterFee)
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
        return BridgeSendTransactionDetails()
    }

    fun fromNetwork(response: BridgeSendTransactionResponse): BridgeSendTransaction {
        return BridgeSendTransaction(
            transaction = response.transaction.toBase64String(),
            message = response.message
        )
    }
}
