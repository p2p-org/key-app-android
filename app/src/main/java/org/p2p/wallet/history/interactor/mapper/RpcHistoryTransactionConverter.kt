package org.p2p.wallet.history.interactor.mapper

import com.google.gson.Gson
import org.p2p.core.utils.Constants.FEE_RELAYER_ACCOUNTS
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.api.model.RpcHistoryFeeResponse
import org.p2p.wallet.history.api.model.RpcHistoryStatusResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionInfoResponse
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.api.model.RpcHistoryTypeResponse
import org.p2p.wallet.history.model.rpc.RpcFee
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.UsernameFormatter
import org.p2p.wallet.utils.fromJsonReified

class RpcHistoryTransactionConverter(
    private val tokenKeyProvider: TokenKeyProvider,
    private val gson: Gson,
    private val usernameFormatter: UsernameFormatter
) {

    fun toDomain(
        transaction: RpcHistoryTransactionResponse,
    ): RpcHistoryTransaction =
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
            RpcHistoryTypeResponse.WORMHOLE_RECEIVE -> parseWormholeReceive(transaction)
            RpcHistoryTypeResponse.WORMHOLE_SEND -> parseWormholeSend(transaction)
            else -> parseUnknown(transaction)
        }

    private fun parseReceive(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Receive>(
            transaction.info.toString()
        )
            ?: error("Parsing error: cannot parse json object ${transaction.info}")

        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()
        return RpcHistoryTransaction.Transfer(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            senderAddress = info.counterParty.address,
            counterPartyUsername = usernameFormatter.formatOrNull(info.counterParty.username),
            iconUrl = info.token.logoUrl,
            amount = RpcHistoryAmount(total, totalInUsd),
            symbol = info.token.symbol.orEmpty(),
            destination = tokenKeyProvider.publicKey,
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseSend(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Send>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.Transfer(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            senderAddress = tokenKeyProvider.publicKey,
            counterPartyUsername = usernameFormatter.formatOrNull(info.counterParty.username),
            iconUrl = info.token.logoUrl,
            amount = RpcHistoryAmount(total, totalInUsd),
            symbol = info.token.symbol.orEmpty(),
            destination = info.counterParty.address,
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseSwap(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Swap>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object ${transaction.info}")
        val sourceTotal = info.from.amounts.amount.toBigDecimalOrZero()
        val sourceTotalInUsd = info.from.amounts.usdAmount.toBigDecimalOrZero()
        val destinationTotal = info.to.amounts.amount.toBigDecimalOrZero()
        val destinationTotalInUsd = info.to.amounts.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.Swap(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            sourceAddress = info.from.token.mint,
            destinationAddress = info.to.token.mint,
            receiveAmount = RpcHistoryAmount(sourceTotal, sourceTotalInUsd),
            sentAmount = RpcHistoryAmount(destinationTotal, destinationTotalInUsd),
            fees = transaction.fees.parseFees(),
            sourceSymbol = info.from.token.symbol.orEmpty(),
            sourceIconUrl = info.from.token.logoUrl,
            destinationSymbol = info.to.token.symbol.orEmpty(),
            destinationIconUrl = info.to.token.logoUrl,
            type = transaction.type.toDomain()
        )
    }

    private fun parseStake(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Stake>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.StakeUnstake(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            senderAddress = tokenKeyProvider.publicKey,
            iconUrl = info.token.logoUrl,
            amount = RpcHistoryAmount(total, totalInUsd),
            symbol = info.token.symbol.orEmpty(),
            destination = info.token.mint,
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseUnstake(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Unstake>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.StakeUnstake(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            amount = RpcHistoryAmount(total, totalInUsd),
            senderAddress = info.token.mint,
            iconUrl = info.token.logoUrl,
            symbol = info.token.symbol.orEmpty(),
            destination = tokenKeyProvider.publicKey,
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseCreate(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.CreateAccount>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.CreateAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            iconUrl = info.token.logoUrl,
            fees = transaction.fees.parseFees(),
            tokenSymbol = info.token.symbol.orEmpty(),
            type = transaction.type.toDomain(),
            amount = RpcHistoryAmount(total, totalInUsd)
        )
    }

    private fun parseClose(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.CloseAccount>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")

        return RpcHistoryTransaction.CloseAccount(
            date = transaction.date.toZonedDateTime(),
            signature = transaction.signature,
            blockNumber = transaction.blockNumber.toInt(),
            account = info.token?.mint.orEmpty(),
            status = transaction.status.toDomain(),
            iconUrl = info.token?.logoUrl,
            tokenSymbol = info.token?.symbol.orEmpty(),
            type = transaction.type.toDomain(),
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseMint(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Mint>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            tokenSymbol = info.token.symbol.orEmpty(),
            iconUrl = info.token.logoUrl,
            type = transaction.type.toDomain(),
            amount = RpcHistoryAmount(total, totalInUsd),
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseBurn(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Burn>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.BurnOrMint(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            tokenSymbol = info.token.symbol.orEmpty(),
            iconUrl = info.token.logoUrl,
            type = transaction.type.toDomain(),
            amount = RpcHistoryAmount(total, totalInUsd),
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseWormholeReceive(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.WormholeReceive>(transaction.info.toString())
        val total = info?.amount?.amount.toBigDecimalOrZero()
        val totalInUsd = info?.amount?.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.WormholeReceive(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            tokenSymbol = info?.tokenAmount?.symbol.orEmpty(),
            amount = RpcHistoryAmount(total, totalInUsd),
            iconUrl = info?.tokenAmount?.logoUrl,
            fees = transaction.fees.parseFees()
        )
    }

    private fun parseWormholeSend(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.WormholeReceive>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")
        val total = info.amount?.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount?.usdAmount.toBigDecimalOrZero()

        return RpcHistoryTransaction.WormholeSend(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            tokenSymbol = info.tokenAmount?.symbol.orEmpty(),
            amount = RpcHistoryAmount(total, totalInUsd),
            iconUrl = info.tokenAmount?.logoUrl,
            fees = transaction.fees.parseFees(),
            sourceAddress = info.to?.address.orEmpty()
        )
    }

    private fun parseUnknown(transaction: RpcHistoryTransactionResponse): RpcHistoryTransaction {
        val info = gson.fromJsonReified<RpcHistoryTransactionInfoResponse.Unknown>(transaction.info.toString())
            ?: error("Parsing error: cannot parse json object  ${transaction.info}")

        val total = info.amount?.amount.toBigDecimalOrZero()
        val totalInUsd = info.amount?.usdAmount.toBigDecimalOrZero()
        return RpcHistoryTransaction.Unknown(
            signature = transaction.signature,
            date = transaction.date.toZonedDateTime(),
            blockNumber = transaction.blockNumber.toInt(),
            status = transaction.status.toDomain(),
            type = transaction.type.toDomain(),
            tokenSymbol = info.token?.symbol.orEmpty(),
            amount = RpcHistoryAmount(total, totalInUsd)
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
        RpcHistoryTypeResponse.WORMHOLE_RECEIVE -> RpcHistoryTransactionType.WORMHOLE_RECEIVE
        RpcHistoryTypeResponse.WORMHOLE_SEND -> RpcHistoryTransactionType.WORMHOLE_SEND
        else -> RpcHistoryTransactionType.UNKNOWN
    }
}

private fun List<RpcHistoryFeeResponse>.parseFees(): List<RpcFee>? {
    return if (this.all { fee -> FEE_RELAYER_ACCOUNTS.contains(fee.payer) }) {
        null
    } else {
        map { fee ->
            val feeInTokens = fee.amount?.amount.toBigDecimalOrZero()
            val feeInFiat = fee.amount?.usdAmount.toBigDecimalOrZero()
            RpcFee(
                totalInTokens = feeInTokens,
                totalInUsd = feeInFiat,
                tokensDecimals = fee.token?.decimals,
                tokenSymbol = fee.token?.symbol
            )
        }
    }
}
