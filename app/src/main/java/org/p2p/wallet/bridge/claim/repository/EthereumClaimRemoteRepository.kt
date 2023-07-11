package org.p2p.wallet.bridge.claim.repository

import androidx.annotation.IntRange
import java.math.BigDecimal
import java.util.Optional
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.api.mapper.BridgeMapper
import org.p2p.wallet.bridge.api.request.GetEthereumBundleRpcRequest
import org.p2p.wallet.bridge.api.request.GetEthereumFeesRpcRequest
import org.p2p.wallet.bridge.api.request.GetEthereumFreeFeeLimitRequest
import org.p2p.wallet.bridge.api.request.GetEthereumBundlesRequest
import org.p2p.wallet.bridge.api.request.SendEthereumBundleRpcRequest
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.model.BridgeBundleFees
import org.p2p.wallet.bridge.repository.BridgeRepository

class EthereumClaimRemoteRepository(
    private val bridgeRepository: BridgeRepository,
    private val mapper: BridgeMapper,
) : EthereumClaimRepository {

    override suspend fun getEthereumFees(
        ethereumAddress: EthAddress,
        recipient: SolAddress,
        tokenAddress: EthAddress?,
        amountAsString: String,
    ): BridgeBundleFees {

        val rpcRequest = GetEthereumFeesRpcRequest(
            ethAddress = ethereumAddress,
            recipientAddress = recipient,
            erc20Token = Optional.ofNullable(tokenAddress),
            amount = amountAsString
        )
        val result = bridgeRepository.launch(rpcRequest)
        return mapper.fromNetwork(result.data)
    }

    private var lastBundle: BridgeBundleResponse? = null
    override suspend fun getEthereumBundle(
        ethAddress: EthAddress,
        recipientAddress: SolAddress,
        erc20Token: EthAddress?,
        amount: String,
        @IntRange(0, 100)
        slippage: Int?,
    ): BridgeBundle {
        val rpcRequest = GetEthereumBundleRpcRequest(
            ethAddress = ethAddress,
            recipientAddress = recipientAddress,
            erc20Token = erc20Token,
            amount = amount,
            slippage = slippage
        )
        val result = bridgeRepository.launch(rpcRequest)
        lastBundle = result.data
        return mapper.fromNetwork(result.data)
    }

    override suspend fun sendEthereumBundle(signatures: List<Signature>) {
        val signedBundle = lastBundle ?: return
        signedBundle.signatures = signatures
        val rpcRequest = SendEthereumBundleRpcRequest(
            bundleRequest = signedBundle
        )
        val result = bridgeRepository.launch(rpcRequest)
        return result.data
    }

    override suspend fun getEthereumBundles(
        ethAddress: EthAddress,
    ): List<BridgeBundle> {
        val rpcRequest = GetEthereumBundlesRequest(
            ethAddress = ethAddress
        )
        val result = bridgeRepository.launch(rpcRequest)
        return result.data.map { mapper.fromNetwork(it) }
    }

    override suspend fun getEthereumMinAmountForFreeFee(): BigDecimal {
        val rpcRequest = GetEthereumFreeFeeLimitRequest
        val result = bridgeRepository.launch(rpcRequest)
        return result.data
    }
}
