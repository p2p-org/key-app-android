package org.p2p.wallet.bridge.api.mapper

import org.p2p.core.utils.orZero
import org.p2p.wallet.bridge.api.response.BridgeBundleFeesResponse
import org.p2p.wallet.bridge.api.response.BridgeBundleFeeResponse
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
            token = response.erc20TokenAddress,
            expiresAt = response.expiresAt ?: System.currentTimeMillis(),
            transactions = response.transactions,
            signatures = response.signatures.orEmpty(),
            fees = fromNetwork(response.fees)
        )
    }

    fun fromNetwork(response: BridgeBundleFeesResponse): BridgeBundleFees {
        return BridgeBundleFees(
            gasEth = fromNetwork(response.gasFee),
            arbiterFee = fromNetwork(response.arbiterFee),
            createAccount = fromNetwork(response.createAccountFee)
        )
    }

    fun fromNetwork(response: BridgeBundleFeeResponse?): BridgeBundleFee {
        return BridgeBundleFee(
            amount = response?.amount.orZero(),
            amountInUsd = response?.usdAmount.orZero()
        )
    }
}
