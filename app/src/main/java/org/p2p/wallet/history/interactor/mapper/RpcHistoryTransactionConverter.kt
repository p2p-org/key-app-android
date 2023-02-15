package org.p2p.wallet.history.interactor.mapper

import kotlinx.coroutines.withContext
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.api.model.RpcHistoryStatusResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionInfoResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.api.model.RpcHistoryTypeResponse
import org.p2p.wallet.history.model.RenBtcType
import org.p2p.wallet.history.model.rpc.BurnOrMint
import org.p2p.wallet.history.model.rpc.CloseAccount
import org.p2p.wallet.history.model.rpc.CreateAccount
import org.p2p.wallet.history.model.rpc.HistoryTransaction
import org.p2p.wallet.history.model.rpc.Swap
import org.p2p.wallet.history.model.rpc.Transfer
import org.p2p.wallet.history.model.rpc.TransferType
import org.p2p.wallet.history.model.rpc.Unknown
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.ZonedDateTime

class RpcHistoryTransactionConverter(
    private val dispatchers: CoroutineDispatchers,
    private val tokenKeyProvider: TokenKeyProvider
) {
    suspend fun mapTransactionDetailsToHistoryTransactions(
        transactions: List<RpcHistoryTransactionResponse>,
    ): List<HistoryTransaction> = withContext(dispatchers.io) {
        transactions.mapNotNull { transaction ->
            when (transaction.type) {
                RpcHistoryTypeResponse.SEND -> parseSend(transaction)
                RpcHistoryTypeResponse.RECEIVE -> parseReceive(transaction)
                RpcHistoryTypeResponse.SWAP -> parseSwap(transaction)
                RpcHistoryTypeResponse.STAKE -> parseStake(transaction)
                RpcHistoryTypeResponse.UNSTAKE -> parseUnstake(transaction)
                RpcHistoryTypeResponse.CREATE_ACCOUNT -> parseCreate(transaction)
                RpcHistoryTypeResponse.CLOSE_ACCOUNT -> parseClose(transaction)
                RpcHistoryTypeResponse.MINT -> parseMint(transaction)
                RpcHistoryTypeResponse.BURN -> parseBurn(transaction)
                RpcHistoryTypeResponse.UNKNOWN -> parseUnknown(transaction)
            }
        }.sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun parseReceive(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Receive
        return Transfer(
            signature = transaction.signature,
            date = ZonedDateTime.parse(transaction.date),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = TransferType.RECEIVE,
            senderAddress = info.counterParty.address,
            iconUrl = info.token.logoUrl.orEmpty(),
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            symbol = info.token.symbol.orEmpty(),
            total = info.amount.amount.toBigDecimalOrZero(),
            destination = tokenKeyProvider.publicKey,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseSend(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Send
        return Transfer(
            signature = transaction.signature,
            date = ZonedDateTime.parse(transaction.date),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = TransferType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl.orEmpty(),
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            symbol = info.token.symbol.orEmpty(),
            total = info.amount.amount.toBigDecimalOrZero(),
            destination = info.counterParty.address,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseSwap(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Swap
        return Swap(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            sourceAddress = info.from.token.mint,
            destinationAddress = info.to.token.mint,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger(),
            amountA = info.from.amount.amount.toBigDecimalOrZero(),
            amountB = info.to.amount.amount.toBigDecimalOrZero(),
            amountSentInUsd = info.from.amount.usdAmount.toBigDecimalOrZero(),
            amountReceivedInUsd = info.to.amount.usdAmount.toBigDecimalOrZero(),
            sourceSymbol = info.from.token.symbol.orEmpty(),
            sourceIconUrl = info.from.token.logoUrl.orEmpty(),
            destinationSymbol = info.to.token.symbol.orEmpty(),
            destinationIconUrl = info.to.token.logoUrl.orEmpty()
        )
    }

    private fun parseStake(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Stake
        return Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain()
        )
    }

    private fun parseUnstake(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        return Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain()
        )
    }

    private fun parseCreate(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.CreateAccount
        return CreateAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            iconUrl = info.token.logoUrl.orEmpty(),
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger(),
            tokenSymbol = transaction.info.token.symbol.orEmpty()
        )
    }

    private fun parseClose(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.CloseAccount
        return CloseAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            account = info.token?.mint.orEmpty(),
            status = transaction.status.toDomain(),
            iconUrl = info.token?.logoUrl.orEmpty(),
            tokenSymbol = transaction.info.token?.symbol.orEmpty()
        )
    }

    private fun parseMint(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Mint
        return BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            destination = info.token.mint,
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl.orEmpty(),
            type = RenBtcType.MINT,
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            total = info.amount.amount.toBigDecimalOrZero(),
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseBurn(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Burn
        return BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            destination = info.token.mint,
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl.orEmpty(),
            type = RenBtcType.BURN,
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            total = info.amount.amount.toBigDecimalOrZero(),
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseUnknown(transaction: RpcHistoryTransactionResponse): HistoryTransaction {
        val info = transaction.info as RpcHistoryTransactionInfoResponse.Unknown
        return Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain()
        )
    }
}

private fun RpcHistoryStatusResponse.toDomain(): TransactionStatus {
    return when (this) {
        RpcHistoryStatusResponse.SUCCESS -> TransactionStatus.COMPLETED
        RpcHistoryStatusResponse.FAIL -> TransactionStatus.ERROR
    }
}
