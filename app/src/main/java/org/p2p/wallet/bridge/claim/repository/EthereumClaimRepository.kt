package org.p2p.wallet.bridge.claim.repository

import java.math.BigDecimal
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.Signature
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
        signatures: List<Signature>,
    )

    suspend fun getEthereumBundles(
        ethAddress: EthAddress
    ): List<BridgeBundle>

    suspend fun getEthereumMinAmountForFreeFee(): BigDecimal
}
