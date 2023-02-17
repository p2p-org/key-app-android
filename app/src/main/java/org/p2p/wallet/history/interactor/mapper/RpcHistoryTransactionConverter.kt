package org.p2p.wallet.history.interactor.mapper

import com.google.gson.Gson
import org.threeten.bp.ZonedDateTime
import kotlinx.coroutines.withContext
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.api.model.RpcHistoryStatusResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionInfoResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.api.model.RpcHistoryTypeResponse
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.fromJsonReified

class RpcHistoryTransactionConverter(
    private val dispatchers: CoroutineDispatchers,
    private val tokenKeyProvider: TokenKeyProvider,
    private val gson: Gson
) {
    suspend fun toDomain(
        transactions: List<RpcHistoryTransactionResponse>,
    ): List<RpcHistoryTransaction> = withContext(dispatchers.io) {
        transactions.map { transaction ->
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

    private fun parseReceive(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info =
            gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Receive>(transaction.info.toString())
                ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.Transfer(
            signature = transaction.signature,
            date = ZonedDateTime.parse(transaction.date),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            senderAddress = info.counterParty.address,
            iconUrl = info.token.logoUrl,
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            symbol = info.token.symbol.orEmpty(),
            total = info.amount.amount.toBigDecimalOrZero(),
            destination = tokenKeyProvider.publicKey,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseSend(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Send>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.Transfer(
            signature = transaction.signature,
            date = ZonedDateTime.parse(transaction.date),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl,
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            symbol = info.token.symbol.orEmpty(),
            total = info.amount.amount.toBigDecimalOrZero(),
            destination = info.counterParty.address,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseSwap(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Swap>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.Swap(
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
            sourceIconUrl = info.from.token.logoUrl,
            destinationSymbol = info.to.token.symbol.orEmpty(),
            destinationIconUrl = info.to.token.logoUrl,
            type = transaction.type.toDomain()
        )
    }

    private fun parseStake(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Stake>(transaction.info.toString())

        return RpcHistoryTransaction.Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain()
        )
    }

    private fun parseUnstake(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        return RpcHistoryTransaction.Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain()
        )
    }

    private fun parseCreate(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.CreateAccount>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.CreateAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            iconUrl = info.token.logoUrl,
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger(),
            tokenSymbol = info.token.symbol.orEmpty(),
            type = transaction.type.toDomain()
        )
    }

    private fun parseClose(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.CloseAccount>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.CloseAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            account = info.token?.mint.orEmpty(),
            status = transaction.status.toDomain(),
            iconUrl = info.token?.logoUrl,
            tokenSymbol = info.token?.symbol.orEmpty(),
            type = transaction.type.toDomain()
        )
    }

    private fun parseMint(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Mint>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            destination = info.token.mint,
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl,
            type = transaction.type.toDomain(),
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            total = info.amount.amount.toBigDecimalOrZero(),
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseBurn(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Burn>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object")

        return RpcHistoryTransaction.BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            destination = info.token.mint,
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl,
            type = transaction.type.toDomain(),
            totalInUsd = info.amount.usdAmount.toBigDecimalOrZero(),
            total = info.amount.amount.toBigDecimalOrZero(),
            fee = transaction.fees.sumOf { it.amount?.amount.toBigDecimalOrZero() }.toBigInteger()
        )
    }

    private fun parseUnknown(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        return RpcHistoryTransaction.Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain()
        )
    }
}

private fun RpcHistoryStatusResponse.toDomain(): HistoryTransactionStatus {
    return when (this) {
        RpcHistoryStatusResponse.SUCCESS -> HistoryTransactionStatus.COMPLETED
        RpcHistoryStatusResponse.FAIL -> HistoryTransactionStatus.ERROR
    }
}

private fun RpcHistoryTypeResponse.toDomain(): RpcHistoryTransactionType {
    return when (this) {
        RpcHistoryTypeResponse.SEND -> RpcHistoryTransactionType.SEND
        RpcHistoryTypeResponse.RECEIVE -> RpcHistoryTransactionType.RECEIVE
        RpcHistoryTypeResponse.SWAP -> RpcHistoryTransactionType.SWAP
        RpcHistoryTypeResponse.STAKE -> RpcHistoryTransactionType.STAKE
        RpcHistoryTypeResponse.UNSTAKE -> RpcHistoryTransactionType.UNSTAKE
        RpcHistoryTypeResponse.CREATE_ACCOUNT -> RpcHistoryTransactionType.CREATE_ACCOUNT
        RpcHistoryTypeResponse.CLOSE_ACCOUNT -> RpcHistoryTransactionType.CLOSE_ACCOUNT
        RpcHistoryTypeResponse.MINT -> RpcHistoryTransactionType.MINT
        RpcHistoryTypeResponse.BURN -> RpcHistoryTransactionType.BURN
        RpcHistoryTypeResponse.UNKNOWN -> RpcHistoryTransactionType.UNKNOWN
    }
}
