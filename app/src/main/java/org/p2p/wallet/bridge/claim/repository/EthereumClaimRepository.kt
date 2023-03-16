package org.p2p.wallet.bridge.claim.repository

import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFees

interface EthereumClaimRepository {
    suspend fun getEthereumFees(
        ethereumAddress: EthAddress,
        recipient: SolAddress,
        tokenAddress: EthAddress?,
        amountAsString: String,
    ): BridgeBundleFees

    suspend fun getEthereumBundle(
        ethAddress: EthAddress,
        recipientAddress: SolAddress,
        erc20Token: EthAddress?,
        amount: String,
        slippage: Int?,
    ): BridgeBundle

    suspend fun sendEthereumBundle(
        bundle: BridgeBundle,
    )
}
