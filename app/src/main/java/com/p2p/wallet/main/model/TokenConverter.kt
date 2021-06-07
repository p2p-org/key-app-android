package com.p2p.wallet.main.model

import com.p2p.wallet.amount.fromLamports
import com.p2p.wallet.amount.toBigDecimalOrZero
import com.p2p.wallet.main.db.TokenEntity
import com.p2p.wallet.token.model.Status
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.model.TokenProgramAccount
import org.p2p.solanaj.rpc.types.TransferInfoResponse
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal

object TokenConverter {

    fun fromNetwork(
        wallet: ConstWallet,
        account: TokenProgramAccount,
        exchangeRate: BigDecimal,
        decimals: Int,
        bid: BigDecimal
    ): Token {
        val total = account.getTotal(decimals)
        return Token(
            tokenSymbol = wallet.tokenSymbol,
            tokenName = wallet.tokenName,
            iconUrl = wallet.icon,
            publicKey = account.publicKey,
            mintAddress = account.mintAddress,
            price = account.getFormattedPrice(exchangeRate, decimals),
            total = total,
            decimals = decimals,
            walletBinds = bid,
            color = wallet.color,
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
            iconUrl = token.iconUrl,
            price = token.price,
            total = token.total,
            walletBinds = token.walletBinds,
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
            iconUrl = entity.iconUrl,
            price = entity.price,
            total = entity.total,
            walletBinds = entity.walletBinds,
            color = entity.color,
            usdRate = entity.exchangeRate.toBigDecimalOrZero(),
            visibility = TokenVisibility.parse(entity.visibility)
        )
}