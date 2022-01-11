package org.p2p.wallet.history.model

import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.wallet.main.model.TokenPrice
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
    fun fromNetwork(
        response: SwapDetails,
        sourceData: TokenData,
        destinationData: TokenData,
        destinationRate: TokenPrice?,
        sourcePublicKey: String
    ): HistoryTransaction =
        HistoryTransaction.Swap(
            signature = response.signature,
            sourceAddress = sourcePublicKey,
            destinationAddress = response.destination,
            fee = response.fee.toBigInteger(),
            blockNumber = response.slot,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.getBlockTimeInMillis()),
                ZoneId.systemDefault()
            ),
            amountA = response.amountA
                .toBigInteger()
                .fromLamports(sourceData.decimals)
                .scaleLong(),
            amountB = response.amountB
                .toBigInteger()
                .fromLamports(destinationData.decimals)
                .scaleLong(),
            amountReceivedInUsd = destinationRate?.let {
                response.amountB
                    .toBigInteger()
                    .fromLamports(destinationData.decimals)
                    .times(it.price)
                    .scaleShort()
            },
            sourceSymbol = sourceData.symbol,
            sourceTokenUrl = sourceData.iconUrl.orEmpty(),
            destinationSymbol = destinationData.symbol,
            destinationTokenUrl = destinationData.iconUrl.orEmpty()
        )

    /* Burn or mint transaction */
    fun fromNetwork(
        response: BurnOrMintDetails,
        rate: TokenPrice?
    ): HistoryTransaction {
        val date = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(response.getBlockTimeInMillis()),
            ZoneId.systemDefault()
        )

        val amount = BigDecimal(response.amount)
            .scaleMedium()
            .times(rate?.price ?: BigDecimal.ZERO)
            .scaleShort()

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

    /* Transfer transaction */
    fun fromNetwork(
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
            destination = if (isSend) response.destination else publicKey,
            fee = response.fee.toBigInteger(),
            type = if (isSend) TransferType.SEND else TransferType.RECEIVE,
            senderAddress = senderAddress,
            tokenData = tokenData,
            amount = amount,
            total = response.amount.toBigInteger().fromLamports(response.decimals),
            date = date
        )
    }

    /* Create account transaction */
    fun fromNetwork(response: CreateAccountDetails): HistoryTransaction =
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
    fun fromNetwork(response: CloseAccountDetails, symbol: String): HistoryTransaction =
        HistoryTransaction.CloseAccount(
            signature = response.signature,
            blockNumber = response.slot,
            account = response.account,
            mint = response.mint,
            date = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(response.getBlockTimeInMillis()),
                ZoneId.systemDefault()
            ),
            tokenSymbol = symbol
        )

    /* Unknown transaction */
    fun fromNetwork(
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