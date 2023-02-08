package org.p2p.wallet.history.interactor.mapper

import org.p2p.core.token.TokenData
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.RenBtcType
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

class HistoryTransactionConverter {

    /* Swap transaction */
    fun mapSwapTransactionToHistory(
        response: SwapDetails,
        sourceData: TokenData,
        destinationData: TokenData,
        sourceRate: TokenPrice?,
        destinationRate: TokenPrice?,
        sourcePublicKey: String
    ): HistoryTransaction =
        HistoryTransaction.Swap(
            signature = response.signature,
            sourceAddress = sourcePublicKey,
            destinationAddress = response.destination.orEmpty(),
            fee = response.fee.toBigInteger(),
            blockNumber = response.slot,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.blockTimeMillis),
                ZoneId.systemDefault()
            ),
            amountA = response.amountA
                .toBigDecimalFromLamports(sourceData.decimals)
                .scaleLong(),
            amountB = response.amountB
                .toBigDecimalFromLamports(destinationData.decimals)
                .scaleLong(),
            amountSentInUsd = sourceRate?.let {
                response.amountA
                    .toBigDecimalFromLamports(sourceData.decimals)
                    .times(it.price)
                    .scaleShort()
            },
            amountReceivedInUsd = destinationRate?.let {
                response.amountB
                    .toBigDecimalFromLamports(destinationData.decimals)
                    .times(it.price)
                    .scaleShort()
            },
            sourceSymbol = sourceData.symbol,
            sourceIconUrl = sourceData.iconUrl.orEmpty(),
            destinationSymbol = destinationData.symbol,
            destinationIconUrl = destinationData.iconUrl.orEmpty(),
            status = TransactionStatus.from(response)
        )

    private fun String.toBigDecimalFromLamports(decimals: Int): BigDecimal =
        toBigInteger()
            .fromLamports(decimals)

    /* Burn or mint transaction */
    fun mapBurnOrMintTransactionToHistory(
        response: BurnOrMintDetails,
        tokenData: TokenData?,
        userPublicKey: String,
        rate: TokenPrice?
    ): HistoryTransaction {
        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.blockTimeMillis),
            ZoneId.systemDefault()
        )

        val amount = rate?.price?.let {
            BigDecimal(response.uiAmount)
                .scaleMedium()
                .times(it)
                .scaleShort()
        }

        val destination = if (response.account == userPublicKey) response.authority else response.account
        val senderAddress = if (response.account == userPublicKey) response.account else response.authority
        return HistoryTransaction.BurnOrMint(
            signature = response.signature,
            blockNumber = response.slot,
            destination = destination.orEmpty(),
            senderAddress = senderAddress.orEmpty(),
            fee = response.fee.toBigInteger(),
            tokenData = tokenData,
            totalInUsd = amount,
            total = response.uiAmount.toBigDecimalOrZero(),
            date = date,
            type = RenBtcType.BURN,
            status = TransactionStatus.from(response)
        )
    }

    /* Transfer transaction */
    fun mapTransferTransactionToHistory(
        response: TransferDetails,
        tokenData: TokenData,
        directPublicKey: String,
        publicKey: String,
        rate: TokenPrice?
    ): HistoryTransaction {
        val isSend = if (response.isSimpleTransfer) {
            (response.source == directPublicKey && response.destination != publicKey) || response.authority == publicKey
        } else {
            response.authority == publicKey
        }

        val senderAddress = if (isSend) {
            if (response.isSimpleTransfer) directPublicKey else publicKey
        } else {
            response.source
        }
        val amount = rate?.price?.let {
            response.amount.toBigDecimalOrZero()
                .toBigInteger()
                .fromLamports(response.decimals)
                .scaleLong()
                .times(it)
        }

        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.blockTimeMillis),
            ZoneId.systemDefault()
        )
        return HistoryTransaction.Transfer(
            signature = response.signature,
            blockNumber = response.slot,
            destination = if (isSend) response.destination.orEmpty() else publicKey,
            fee = response.fee.toBigInteger(),
            type = if (isSend) TransferType.SEND else TransferType.RECEIVE,
            senderAddress = senderAddress.orEmpty(),
            tokenData = tokenData,
            totalInUsd = amount,
            total = response.amount
                ?.toBigInteger()
                ?.fromLamports(response.decimals)
                ?: BigDecimal.ZERO,
            date = date,
            status = TransactionStatus.from(response)
        )
    }

    /* Create account transaction */
    fun mapCreateAccountTransactionToHistory(
        response: CreateAccountDetails,
        tokenData: TokenData?,
        symbol: String
    ): HistoryTransaction =
        HistoryTransaction.CreateAccount(
            signature = response.signature,
            blockNumber = response.slot,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.blockTimeMillis),
                ZoneId.systemDefault()
            ),
            tokenData = tokenData,
            fee = response.fee.toBigInteger(),
            status = TransactionStatus.from(response),
            tokenSymbol = symbol
        )

    /* Close account transaction */
    fun mapCloseAccountTransactionToHistory(
        response: CloseAccountDetails,
        tokenData: TokenData?,
        symbol: String,
    ): HistoryTransaction =
        HistoryTransaction.CloseAccount(
            signature = response.signature,
            blockNumber = response.slot,
            account = response.account.orEmpty(),
            mint = response.mint.orEmpty(),
            tokenData = tokenData,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.blockTimeMillis),
                ZoneId.systemDefault()
            ),
            tokenSymbol = symbol,
            status = TransactionStatus.from(response)
        )

    /* Unknown transaction */
    fun mapUnknownTransactionToHistory(
        response: UnknownDetails
    ): HistoryTransaction =
        HistoryTransaction.Unknown(
            signature = response.signature,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.blockTimeMillis),
                ZoneId.systemDefault()
            ),
            blockNumber = response.slot,
            status = TransactionStatus.from(response)
        )
}
