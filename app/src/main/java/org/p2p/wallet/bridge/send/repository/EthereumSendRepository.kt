package org.p2p.wallet.bridge.send.repository

import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.model.BridgeSendTransaction
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

interface EthereumSendRepository {
    suspend fun transferFromSolana(
        userWallet: SolAddress,
        feePayer: SolAddress,
        source: SolAddress,
        recipient: EthAddress,
        mint: SolAddress?,
        amount: String
    ): BridgeSendTransaction

    suspend fun getSendTransactionDetail(message: String): BridgeSendTransactionDetails
    suspend fun getSendTransactionsDetail(userWallet: SolAddress): List<BridgeSendTransactionDetails>
    suspend fun getSendFee(
        userWallet: SolAddress,
        recipient: EthAddress,
        mint: SolAddress?,
        amount: String
    ): BridgeSendFees
}
