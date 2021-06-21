package com.p2p.wallet.main.model

import com.p2p.wallet.amount.fromLamports
import com.p2p.wallet.amount.toBigDecimalOrZero
import com.p2p.wallet.main.db.TokenEntity
import com.p2p.wallet.token.model.Status
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.local.TokenResponse
import com.p2p.wallet.user.model.TokenData
import com.p2p.wallet.user.model.TokenProgramAccount
import org.p2p.solanaj.model.types.TransferInfoResponse
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
        data: TokenData,
        account: TokenProgramAccount,
        exchangeRate: BigDecimal,
        bid: BigDecimal,
        color: Int
    ): Token {
        val total = account.getTotal(data.decimals)
        return Token(
            tokenSymbol = data.symbol,
            tokenName = data.name,
            logoUrl = data.iconUrl,
            publicKey = account.publicKey,
            mintAddress = account.mintAddress,
            price = account.getFormattedPrice(exchangeRate, data.decimals),
            total = total,
            decimals = data.decimals,
            color = color,
            usdRate = exchangeRate,
            visibility = TokenVisibility.DEFAULT
        )
    }

    /* todo: validate Swap operation, parse amount and total amount of token, validate status */
    fun fromNetwork(
        response: TransferInfoResponse,
        publicKey: String,
        tokenSymbol: String,
        date: ZonedDateTime
    ): Transaction =
        when {
            response.from == publicKey ->
                Transaction.Send(
                    transactionId = response.signature,
                    destination = response.to,
                    amount = response.lamports.fromLamports(),
                    total = response.lamports.fromLamports(),
                    status = Status.SUCCESS,
                    date = date,
                    tokenSymbol = tokenSymbol
                )
            else ->
                Transaction.Receive(
                    transactionId = response.signature,
                    amount = response.lamports.fromLamports(),
                    total = response.lamports.fromLamports(),
                    status = Status.SUCCESS,
                    date = date,
                    senderAddress = response.from,
                    tokenSymbol = tokenSymbol
                )
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