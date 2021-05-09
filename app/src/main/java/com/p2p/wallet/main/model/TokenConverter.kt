package com.p2p.wallet.main.model

import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.main.api.MultiPriceResponse
import com.p2p.wallet.main.api.SinglePriceResponse
import com.p2p.wallet.token.model.Status
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.model.TokenProgramAccount
import org.p2p.solanaj.rpc.types.TransferInfoResponse
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import kotlin.math.pow

object TokenConverter {

    fun fromNetwork(
        wallet: ConstWallet,
        account: TokenProgramAccount,
        exchangeRate: BigDecimal,
        decimals: Int
    ) = Token(
        tokenSymbol = wallet.tokenSymbol,
        tokenName = wallet.tokenName,
        iconUrl = wallet.icon,
        depositAddress = account.depositAddress,
        mintAddress = account.mintAddress,
        price = account.getFormattedPrice(exchangeRate, decimals),
        total = account.getAmount(decimals),
        decimals = decimals,
        walletBinds = if (wallet.isUS()) 1.0 else 0.0,
        color = wallet.color,
        exchangeRate = exchangeRate
    )

    fun fromNetwork(tokenSymbol: String, response: MultiPriceResponse): TokenPrice = when (tokenSymbol) {
        "SOL" -> TokenPrice(tokenSymbol, getOrZero(response.SOL))
        "BTC" -> TokenPrice(tokenSymbol, getOrZero(response.BTC))
        "SRM" -> TokenPrice(tokenSymbol, getOrZero(response.SRM))
        "MSRM" -> TokenPrice(tokenSymbol, getOrZero(response.MSRM))
        "ETH" -> TokenPrice(tokenSymbol, getOrZero(response.ETH))
        "FTT" -> TokenPrice(tokenSymbol, getOrZero(response.FTT))
        "YFI" -> TokenPrice(tokenSymbol, getOrZero(response.YFI))
        "LINK" -> TokenPrice(tokenSymbol, getOrZero(response.LINK))
        "XRP" -> TokenPrice(tokenSymbol, getOrZero(response.XRP))
        "USDT" -> TokenPrice(tokenSymbol, getOrZero(response.USDT))
        "USDC" -> TokenPrice(tokenSymbol, getOrZero(response.USDC))
        "WUSDC" -> TokenPrice(tokenSymbol, getOrZero(response.WUSDC))
        "SUSHI" -> TokenPrice(tokenSymbol, getOrZero(response.SUSHI))
        "ALEPH" -> TokenPrice(tokenSymbol, getOrZero(response.ALEPH))
        "SXP" -> TokenPrice(tokenSymbol, getOrZero(response.SXP))
        "HGET" -> TokenPrice(tokenSymbol, getOrZero(response.HGET))
        "CREAM" -> TokenPrice(tokenSymbol, getOrZero(response.CREAM))
        "UBXT" -> TokenPrice(tokenSymbol, getOrZero(response.UBXT))
        "HNT" -> TokenPrice(tokenSymbol, getOrZero(response.HNT))
        "FRONT" -> TokenPrice(tokenSymbol, getOrZero(response.FRONT))
        "AKRO" -> TokenPrice(tokenSymbol, getOrZero(response.AKRO))
        "HXRO" -> TokenPrice(tokenSymbol, getOrZero(response.HXRO))
        "UNI" -> TokenPrice(tokenSymbol, getOrZero(response.UNI))
        "MATH" -> TokenPrice(tokenSymbol, getOrZero(response.MATH))
        "TOMO" -> TokenPrice(tokenSymbol, getOrZero(response.TOMO))
        "LUA" -> TokenPrice(tokenSymbol, getOrZero(response.LUA))
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    private fun getOrZero(response: SinglePriceResponse?): BigDecimal = response?.getValue() ?: BigDecimal.ZERO

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
                    amount = BigDecimal(response.lamports.toDouble() / (10.0.pow(9))),
                    total = BigDecimal(response.lamports.toDouble() / (10.0.pow(9))),
                    status = Status.SUCCESS,
                    date = date,
                    tokenSymbol = tokenSymbol
                )
            else ->
                Transaction.Receive(
                    transactionId = response.signature,
                    amount = BigDecimal(response.lamports.toDouble() / (10.0.pow(9))),
                    total = BigDecimal(response.lamports.toDouble() / (10.0.pow(9))),
                    status = Status.SUCCESS,
                    date = date,
                    senderAddress = response.from,
                    tokenSymbol = tokenSymbol
                )
        }
}