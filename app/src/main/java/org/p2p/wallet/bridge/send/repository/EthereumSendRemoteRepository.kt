package org.p2p.wallet.bridge.send.repository

import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.api.mapper.BridgeMapper
import org.p2p.wallet.bridge.api.request.GetSolanaFeesRpcRequest
import org.p2p.wallet.bridge.api.request.SolanaTransferStatusRpcRequest
import org.p2p.wallet.bridge.api.request.SolanaTransferStatusesRpcRequest
import org.p2p.wallet.bridge.api.request.TransferFromSolanaRpcRequest
import org.p2p.wallet.bridge.repository.BridgeRepository
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

class EthereumSendRemoteRepository(
    private val bridgeRepository: BridgeRepository,
    private val mapper: BridgeMapper,
) : EthereumSendRepository {

    override suspend fun transferFromSolana(
        userWallet: SolAddress,
        feePayer: SolAddress,
        source: SolAddress,
        recipient: EthAddress,
        mint: SolAddress?,
        amount: String,
    ): BridgeSendTransaction {
        val request = TransferFromSolanaRpcRequest(
            userWallet = userWallet,
            feePayer = feePayer,
            source = source,
            recipient = recipient,
            mint = mint,
            amount = amount
        )
        val result = bridgeRepository.launch(request)
        return mapper.fromNetwork(result.data)
    }

    override suspend fun getSendTransactionDetail(message: String): BridgeSendTransactionDetails {
        val request = SolanaTransferStatusRpcRequest(message = message)
        val result = bridgeRepository.launch(request)
        return mapper.fromNetwork(result.data)
    }

    override suspend fun getSendTransactionsDetail(userWallet: SolAddress): List<BridgeSendTransactionDetails> {
        val request = SolanaTransferStatusesRpcRequest(userWallet)
        val result = bridgeRepository.launch(request)
        return result.data.map { mapper.fromNetwork(it) }
    }

    override suspend fun getSendFee(
        userWallet: SolAddress,
        recipient: EthAddress,
        mint: SolAddress?,
        amount: String,
    ): BridgeSendFees {
        val request = GetSolanaFeesRpcRequest(userWallet, recipient, mint, amount)
        val result = bridgeRepository.launch(request)
        return mapper.fromNetwork(result.data)
    }
}
