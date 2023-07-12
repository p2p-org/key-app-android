package org.p2p.wallet.home.ui.main.striga

import org.threeten.bp.ZonedDateTime
import org.p2p.core.utils.emptyString
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

/**
 * If user confirmed claim using OTP - we add claim transaction as pending
 */
class StrigaClaimConfirmedHandler(
    private val historyInteractor: HistoryInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {
    suspend fun handleClaimConfirmed(claimedToken: HomeElementItem.StrigaClaim) {
        addClaimPendingTransaction(claimedToken)
    }

    private suspend fun addClaimPendingTransaction(claimedToken: HomeElementItem.StrigaClaim) {
        historyInteractor.addPendingTransaction(
            txSignature = RpcHistoryTransaction.Transfer.STRIGA_CLAIM_TX_ID,
            mintAddress = claimedToken.tokenMintAddress,
            transaction = createReceiveTransaction(claimedToken)
        )
    }

    private fun createReceiveTransaction(claimedToken: HomeElementItem.StrigaClaim): RpcHistoryTransaction.Transfer {
        return RpcHistoryTransaction.Transfer(
            signature = emptyString(),
            date = ZonedDateTime.now(),
            blockNumber = 0,
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.RECEIVE,
            senderAddress = RpcHistoryTransaction.Transfer.STRIGA_CLAIM_SENDER_ADDRESS,
            iconUrl = claimedToken.tokenIcon,
            amount = claimedToken.amountAvailable.let { RpcHistoryAmount(it, it) },
            symbol = claimedToken.tokenSymbol,
            destination = tokenKeyProvider.publicKey,
            counterPartyUsername = null,
            fees = null
        )
    }
}
