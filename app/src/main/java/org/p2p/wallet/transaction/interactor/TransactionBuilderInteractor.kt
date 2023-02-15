package org.p2p.wallet.transaction.interactor

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.core.token.Token
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toUsd
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.threeten.bp.ZonedDateTime
import java.math.BigInteger

class TransactionBuilderInteractor {

    fun buildTransaction(
        source: Token.Active,
        destination: Token,
        sourceAmount: String,
        destinationAmount: String,
        transactionId: String,
        destinationAddress: String
    ): HistoryTransaction {
        val amountA = sourceAmount.toBigDecimalOrZero()
        val amountB = destinationAmount.toBigDecimalOrZero()
        return RpcHistoryTransaction.Swap(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            sourceAddress = source.publicKey,
            destinationAddress = destinationAddress,
            fee = BigInteger.ZERO,
            amountA = amountA,
            amountB = amountB,
            amountSentInUsd = amountA.toUsd(source),
            amountReceivedInUsd = amountB.toUsd(destination.rate),
            sourceSymbol = source.tokenSymbol,
            sourceIconUrl = source.iconUrl.orEmpty(),
            destinationSymbol = destination.tokenSymbol,
            destinationIconUrl = destination.iconUrl.orEmpty(),
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.SWAP
        )
    }
}
