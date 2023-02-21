package org.p2p.wallet.transaction.interactor

import org.threeten.bp.ZonedDateTime
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toUsd
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

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
            fee = BigInteger.ZERO.toString(),
            sentAmount = RpcHistoryAmount(amountA, amountA.toUsd(source)),
            receiveAmount = RpcHistoryAmount(amountB, amountB.toUsd(destination.rate)),
            sourceSymbol = source.tokenSymbol,
            sourceIconUrl = source.iconUrl.orEmpty(),
            destinationSymbol = destination.tokenSymbol,
            destinationIconUrl = destination.iconUrl.orEmpty(),
            status = HistoryTransactionStatus.PENDING,
            type = RpcHistoryTransactionType.SWAP
        )
    }
}
