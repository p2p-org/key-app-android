package org.p2p.wallet.history.repository

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
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

object TransactionConverter {

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
                Instant.ofEpochMilli(response.getBlockTimeInMillis()),
                ZoneId.systemDefault()
            ),
            amountA = response.amountA
                ?.toBigInteger()
                ?.fromLamports(sourceData.decimals)
                ?.scaleLong()
                ?: BigDecimal.ZERO,
            amountB = response.amountB
                ?.toBigInteger()
                ?.fromLamports(destinationData.decimals)
                ?.scaleLong()
                ?: BigDecimal.ZERO,
            amountSentInUsd = sourceRate?.let {
                response.amountA
                    ?.toBigInteger()
                    ?.fromLamports(sourceData.decimals)
                    ?.times(it.price)
                    ?.scaleShort()
                    ?: BigDecimal.ZERO
            },
            amountReceivedInUsd = destinationRate?.let {
                response.amountB
                    ?.toBigInteger()
                    ?.fromLamports(destinationData.decimals)
                    ?.times(it.price)
                    ?.scaleShort()
                    ?: BigDecimal.ZERO
            },
            sourceSymbol = sourceData.symbol,
            sourceIconUrl = sourceData.iconUrl.orEmpty(),
            destinationSymbol = destinationData.symbol,
            destinationIconUrl = destinationData.iconUrl.orEmpty()
        )

    /* Burn or mint transaction */
    fun mapBurnOrMintTransactionToHistory(
        response: BurnOrMintDetails,
        userPublicKey: String,
        rate: TokenPrice?
    ): HistoryTransaction {
        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.getBlockTimeInMillis()),
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
            totalInUsd = amount,
            total = response.uiAmount?.toBigDecimalOrZero() ?: BigDecimal.ZERO,
            date = date,
            type = RenBtcType.BURN
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
            response.source == directPublicKey && response.destination != publicKey
        } else {
            response.authority == publicKey
        }

        val senderAddress = if (isSend) {
            if (response.isSimpleTransfer) directPublicKey else publicKey
        } else {
            response.source
        }
        val amount = rate?.price?.let {
            BigDecimal(response.amount).toBigInteger()
                .fromLamports(response.decimals)
                .scaleLong()
                .times(it)
        }

        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.getBlockTimeInMillis()),
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
            total = response.amount?.toBigInteger()?.fromLamports(response.decimals) ?: BigDecimal.ZERO,
            date = date
        )
    }

    /* Create account transaction */
    fun mapCreateAccountTransactionToHistory(response: CreateAccountDetails): HistoryTransaction =
        HistoryTransaction.CreateAccount(
            signature = response.signature,
            blockNumber = response.slot,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.getBlockTimeInMillis()),
                ZoneId.systemDefault()
            ),
            fee = response.fee.toBigInteger()
        )

    /* Close account transaction */
    fun mapCloseAccountTransactionToHistory(response: CloseAccountDetails, symbol: String): HistoryTransaction =
        HistoryTransaction.CloseAccount(
            signature = response.signature,
            blockNumber = response.slot,
            account = response.account.orEmpty(),
            mint = response.mint.orEmpty(),
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.getBlockTimeInMillis()),
                ZoneId.systemDefault()
            ),
            tokenSymbol = symbol
        )

    /* Unknown transaction */
    fun mapUnknownTransactionToHistory(
        response: UnknownDetails
    ): HistoryTransaction =
        HistoryTransaction.Unknown(
            signature = response.signature,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.getBlockTimeInMillis()), ZoneId.systemDefault()
            ),
            blockNumber = response.slot,
        )
}