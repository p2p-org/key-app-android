package org.p2p.wallet.home.ui.main.striga

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.UUID
import org.p2p.core.utils.emptyString
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction.Companion.UNDEFINED_BLOCK_NUMBER
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

/**
 * If user confirmed claim using OTP - we add claim transaction as pending
 */
class StrigaOnRampConfirmedHandler(
    private val historyInteractor: HistoryInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val strigaUserWalletInteractor: StrigaWalletInteractor
) {
    suspend fun handleConfirmed(claimedToken: HomeElementItem.StrigaOnRampTokenItem) {
        kotlin.runCatching {
            addOnRampPendingTransaction(claimedToken)
        }.onFailure { Timber.e(it, "Failed to add pending transaction on onramp") }
    }

    private suspend fun addOnRampPendingTransaction(onRampTokenItem: HomeElementItem.StrigaOnRampTokenItem) {
        val strigaUserCryptoAddress = strigaUserWalletInteractor.getCryptoAccountDetails().depositAddress

        historyInteractor.addPendingTransaction(
            txSignature = emptyString(),
            mintAddress = onRampTokenItem.tokenMintAddress,
            transaction = createReceiveTransaction(onRampTokenItem, strigaUserCryptoAddress)
        )
    }

    private fun createReceiveTransaction(
        claimedToken: HomeElementItem.StrigaOnRampTokenItem,
        strigaUserCryptoAddress: String
    ): RpcHistoryTransaction.Transfer {
        return RpcHistoryTransaction.Transfer(
            // no signature available, so randomize it
            signature = UUID.randomUUID().toString(),
            date = ZonedDateTime.now(),
            blockNumber = UNDEFINED_BLOCK_NUMBER,
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.RECEIVE,
            senderAddress = strigaUserCryptoAddress,
            iconUrl = claimedToken.tokenIcon,
            amount = claimedToken.amountAvailable.let { RpcHistoryAmount(it, it) },
            symbol = claimedToken.tokenSymbol,
            destination = tokenKeyProvider.publicKey,
            counterPartyUsername = null,
            fees = null
        )
    }
}
