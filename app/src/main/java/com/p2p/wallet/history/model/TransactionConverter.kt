package com.p2p.wallet.history.model

import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.TokenData
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toPowerValue
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

object TransactionConverter {

    fun fromNetwork(
        response: SwapDetails,
        sourceData: TokenData,
        destinationData: TokenData,
        destinationRate: TokenPrice,
        sourcePublicKey: String
    ): HistoryTransaction =
        HistoryTransaction.Swap(
            signature = response.signature,
            sourceAddress = sourcePublicKey,
            destinationAddress = response.destination,
            fee = response.fee.toBigInteger(),
            blockNumber = response.slot,
            date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(response.blockTime), ZoneId.systemDefault()),
            amountA = response.amountA
                .toBigInteger()
                .fromLamports(sourceData.decimals)
                .scaleMedium(),
            amountB = response.amountB
                .toBigInteger()
                .fromLamports(destinationData.decimals)
                .scaleMedium(),
            amountReceivedInUsd = response.amountB
                .toBigInteger()
                .fromLamports(destinationData.decimals)
                .times(destinationRate.price)
                .scaleMedium(),
            sourceSymbol = sourceData.symbol,
            sourceTokenUrl = sourceData.iconUrl.orEmpty(),
            destinationSymbol = destinationData.symbol,
            destinationTokenUrl = destinationData.iconUrl.orEmpty()
        )

    fun fromNetwork(
        response: BurnOrMintDetails,
        rate: TokenPrice
    ): HistoryTransaction {
        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.blockTime),
            ZoneId.systemDefault()
        )

        val amount = BigDecimal(response.amount)
            .scaleMedium()
            .times(rate.price)
            .scaleMedium()

        return HistoryTransaction.BurnOrMint(
            signature = response.signature,
            blockNumber = response.slot,
            fee = response.fee.toBigInteger(),
            amount = amount,
            total = response.amount.toBigDecimalOrZero(),
            date = date,
            type = RenBtcType.BURN
        )
    }

    fun fromNetwork(
        response: TransferDetails,
        tokenData: TokenData,
        directPublicKey: String,
        publicKey: String,
        rate: TokenPrice
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
        val amount = BigDecimal(response.amount).toBigInteger()
            .fromLamports(response.decimals)
            .scaleMedium()
            .times(rate.price)

        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.blockTime),
            ZoneId.systemDefault()
        )
        return HistoryTransaction.Transfer(
            signature = response.signature,
            blockNumber = response.slot,
            destination = if (isSend) response.destination else publicKey,
            fee = response.fee.toBigInteger(),
            type = if (isSend) TransferType.SEND else TransferType.RECEIVE,
            senderAddress = senderAddress,
            tokenData = tokenData,
            amount = amount,
            total = BigDecimal(response.amount).divide(response.decimals.toPowerValue()),
            date = date
        )
    }

    fun fromNetwork(response: CloseAccountDetails, symbol: String): HistoryTransaction =
        HistoryTransaction.CloseAccount(
            signature = response.signature,
            blockNumber = response.slot,
            account = response.account,
            destination = response.destination,
            owner = response.owner,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.blockTime),
                ZoneId.systemDefault()
            ),
            tokenSymbol = symbol
        )

    fun fromNetwork(
        response: UnknownDetails
    ): HistoryTransaction =
        HistoryTransaction.Unknown(
            signature = response.signature,
            date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(response.blockTime), ZoneId.systemDefault()),
            blockNumber = response.slot,
        )
}