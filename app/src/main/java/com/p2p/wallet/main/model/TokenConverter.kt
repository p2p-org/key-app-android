package com.p2p.wallet.main.model

import com.p2p.wallet.amount.fromLamports
import com.p2p.wallet.amount.toBigDecimalOrZero
import com.p2p.wallet.amount.toPowerValue
import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.db.TokenEntity
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.local.TokenResponse
import com.p2p.wallet.user.model.TokenData
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.model.types.Account
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

object TokenConverter {

    fun fromLocal(response: TokenResponse): TokenData =
        TokenData(
            mintAddress = response.address,
            name = response.name,
            symbol = response.symbol,
            iconUrl = response.logoUrl,
            decimals = response.decimals
        )

    fun fromNetwork(
        account: Account,
        tokenData: TokenData,
        price: TokenPrice
    ): Token {
        val data = account.account.data
        val mintAddress = data.parsed.info.mint
        val total = data.parsed.info.tokenAmount.amount.toBigInteger()
        return Token(
            publicKey = account.pubkey,
            mintAddress = mintAddress,
            tokenSymbol = tokenData.symbol,
            decimals = tokenData.decimals,
            tokenName = tokenData.name,
            logoUrl = tokenData.iconUrl,
            price = total.fromLamports(tokenData.decimals).times(price.price),
            total = BigDecimal(total).divide(tokenData.decimals.toPowerValue()),
            color = TokenColors.findColorBySymbol(tokenData.symbol),
            usdRate = price.price,
            visibility = TokenVisibility.DEFAULT
        )
    }

    fun fromNetwork(
        response: TransactionDetails?,
        publicKey: String,
        symbol: String,
        rate: TokenPrice
    ): Transaction? =
        when (response) {
            is SwapDetails ->
                Transaction.Swap(
                    response.signature,
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(response.blockTime), ZoneId.systemDefault()),
                    symbol,
                    response.amountA.toBigInteger().fromLamports(),
                    response.amountB.toBigInteger().fromLamports(),
                    response.mintA,
                    response.mintB
                )
            is TransferDetails ->
                if (response.source == publicKey) {
                    Transaction.Send(
                        transactionId = response.signature,
                        destination = response.destination,
                        amount = response.amount.toBigInteger().fromLamports(response.decimals).times(rate.price),
                        total = BigDecimal(response.amount).divide(response.decimals.toPowerValue()),
                        date = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(response.blockTime),
                            ZoneId.systemDefault()
                        ),
                        tokenSymbol = symbol
                    )
                } else {
                    Transaction.Receive(
                        transactionId = response.signature,
                        amount = response.amount.toBigInteger().fromLamports(response.decimals).times(rate.price),
                        total = BigDecimal(response.amount).divide(response.decimals.toPowerValue()),
                        date = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(response.blockTime),
                            ZoneId.systemDefault()
                        ),
                        senderAddress = response.source,
                        tokenSymbol = symbol
                    )
                }

            is CloseAccountDetails ->
                Transaction.CloseAccount(
                    transactionId = response.signature,
                    account = response.account,
                    destination = response.destination,
                    owner = response.owner,
                    date = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(response.blockTime),
                        ZoneId.systemDefault()
                    ),
                    tokenSymbol = symbol
                )
            is UnknownDetails ->
                Transaction.Unknown(
                    response.signature,
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(response.blockTime), ZoneId.systemDefault()),
                    symbol
                )
            else -> null
        }

    fun toDatabase(token: Token): TokenEntity =
        TokenEntity(
            tokenSymbol = token.tokenSymbol,
            publicKey = token.publicKey,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.logoUrl,
            price = token.price,
            total = token.total,
            color = token.color,
            exchangeRate = token.usdRate.toString(),
            visibility = token.visibility.stringValue
        )

    fun fromDatabase(entity: TokenEntity): Token =
        Token(
            tokenSymbol = entity.tokenSymbol,
            publicKey = entity.publicKey,
            decimals = entity.decimals,
            mintAddress = entity.mintAddress,
            tokenName = entity.tokenName,
            logoUrl = entity.iconUrl,
            price = entity.price,
            total = entity.total,
            color = entity.color,
            usdRate = entity.exchangeRate.toBigDecimalOrZero(),
            visibility = TokenVisibility.parse(entity.visibility)
        )
}