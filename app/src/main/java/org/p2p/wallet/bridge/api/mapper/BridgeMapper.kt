package org.p2p.wallet.bridge.api.mapper

import org.p2p.wallet.bridge.api.response.BridgeBundleFeeResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFee
import org.p2p.wallet.bridge.model.BridgeBundleFees

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

    fun fromNetwork(response: BridgeBundleFeeResponse?): BridgeBundleFee {
        return BridgeBundleFee(
            amount = response?.amount,
            amountInUsd = response?.usdAmount,
            chain = response?.chain,
            token = response?.tokenName
        )
    }
}
