package org.p2p.wallet.transaction.interactor

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toUsd
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
        return HistoryTransaction.Swap(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            sourceAddress = source.publicKey,
            destinationAddress = destinationAddress,
            fee = BigInteger.ZERO,
            amountA = amountA,
            amountB = amountB,
            amountSentInUsd = amountA.toUsd(source),
            amountReceivedInUsd = amountB.toUsd(destination.usdRate),
            sourceSymbol = source.tokenSymbol,
            sourceIconUrl = source.iconUrl.orEmpty(),
            destinationSymbol = destination.tokenSymbol,
            destinationIconUrl = destination.iconUrl.orEmpty(),
            status = TransactionStatus.PENDING
        )
    }
}
