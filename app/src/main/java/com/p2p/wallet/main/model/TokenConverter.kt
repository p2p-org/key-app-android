package com.p2p.wallet.main.model

import com.p2p.wallet.amount.valueOrZero
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
        exchangeRate: Double,
        decimals: Int
    ) = Token(
        tokenSymbol = wallet.tokenSymbol,
        tokenName = wallet.tokenName,
        iconUrl = wallet.icon,
        publicKey = account.publicKey,
        mintAddress = account.mintAddress,
        price = account.getFormattedPrice(exchangeRate, decimals),
        total = account.getAmount(decimals),
        decimals = decimals,
        walletBinds = if (wallet.isUS()) 1.0 else 0.0,
        color = wallet.color,
        exchangeRate = exchangeRate
    )

    fun fromNetwork(tokenSymbol: String, response: MultiPriceResponse): TokenPrice = when (tokenSymbol) {
        "SOL" -> TokenPrice(tokenSymbol, usdOrZero(response.SOL))
        "BTC" -> TokenPrice(tokenSymbol, usdOrZero(response.BTC))
        "SRM" -> TokenPrice(tokenSymbol, usdOrZero(response.SRM))
        "MSRM" -> TokenPrice(tokenSymbol, usdOrZero(response.MSRM))
        "ETH" -> TokenPrice(tokenSymbol, usdOrZero(response.ETH))
        "FTT" -> TokenPrice(tokenSymbol, usdOrZero(response.FTT))
        "YFI" -> TokenPrice(tokenSymbol, usdOrZero(response.YFI))
        "LINK" -> TokenPrice(tokenSymbol, usdOrZero(response.LINK))
        "XRP" -> TokenPrice(tokenSymbol, usdOrZero(response.XRP))
        "USDT" -> TokenPrice(tokenSymbol, usdOrZero(response.USDT))
        "USDC" -> TokenPrice(tokenSymbol, usdOrZero(response.USDC))
        "WUSDC" -> TokenPrice(tokenSymbol, usdOrZero(response.WUSDC))
        "SUSHI" -> TokenPrice(tokenSymbol, usdOrZero(response.SUSHI))
        "ALEPH" -> TokenPrice(tokenSymbol, usdOrZero(response.ALEPH))
        "SXP" -> TokenPrice(tokenSymbol, usdOrZero(response.SXP))
        "HGET" -> TokenPrice(tokenSymbol, usdOrZero(response.HGET))
        "CREAM" -> TokenPrice(tokenSymbol, usdOrZero(response.CREAM))
        "UBXT" -> TokenPrice(tokenSymbol, usdOrZero(response.UBXT))
        "HNT" -> TokenPrice(tokenSymbol, usdOrZero(response.HNT))
        "FRONT" -> TokenPrice(tokenSymbol, usdOrZero(response.FRONT))
        "AKRO" -> TokenPrice(tokenSymbol, usdOrZero(response.AKRO))
        "HXRO" -> TokenPrice(tokenSymbol, usdOrZero(response.HXRO))
        "UNI" -> TokenPrice(tokenSymbol, usdOrZero(response.UNI))
        "MATH" -> TokenPrice(tokenSymbol, usdOrZero(response.MATH))
        "TOMO" -> TokenPrice(tokenSymbol, usdOrZero(response.TOMO))
        "LUA" -> TokenPrice(tokenSymbol, usdOrZero(response.LUA))
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    fun fromNetwork(tokenSymbol: String, response: SinglePriceResponse): TokenPrice = when (tokenSymbol) {
        "USD" -> TokenPrice(tokenSymbol, response.usdValue.valueOrZero())
        "SOL" -> TokenPrice(tokenSymbol, response.SOL.valueOrZero())
        "BTC" -> TokenPrice(tokenSymbol, response.BTC.valueOrZero())
        "SRM" -> TokenPrice(tokenSymbol, response.SRM.valueOrZero())
        "MSRM" -> TokenPrice(tokenSymbol, response.MSRM.valueOrZero())
        "ETH" -> TokenPrice(tokenSymbol, response.ETH.valueOrZero())
        "FTT" -> TokenPrice(tokenSymbol, response.FTT.valueOrZero())
        "YFI" -> TokenPrice(tokenSymbol, response.YFI.valueOrZero())
        "LINK" -> TokenPrice(tokenSymbol, response.LINK.valueOrZero())
        "XRP" -> TokenPrice(tokenSymbol, response.XRP.valueOrZero())
        "USDT" -> TokenPrice(tokenSymbol, response.USDT.valueOrZero())
        "USDC" -> TokenPrice(tokenSymbol, response.USDC.valueOrZero())
        "WUSDC" -> TokenPrice(tokenSymbol, response.WUSDC.valueOrZero())
        "SUSHI" -> TokenPrice(tokenSymbol, response.SUSHI.valueOrZero())
        "ALEPH" -> TokenPrice(tokenSymbol, response.ALEPH.valueOrZero())
        "SXP" -> TokenPrice(tokenSymbol, response.SXP.valueOrZero())
        "HGET" -> TokenPrice(tokenSymbol, response.HGET.valueOrZero())
        "CREAM" -> TokenPrice(tokenSymbol, response.CREAM.valueOrZero())
        "UBXT" -> TokenPrice(tokenSymbol, response.UBXT.valueOrZero())
        "HNT" -> TokenPrice(tokenSymbol, response.HNT.valueOrZero())
        "FRONT" -> TokenPrice(tokenSymbol, response.FRONT.valueOrZero())
        "AKRO" -> TokenPrice(tokenSymbol, response.AKRO.valueOrZero())
        "HXRO" -> TokenPrice(tokenSymbol, response.HXRO.valueOrZero())
        "UNI" -> TokenPrice(tokenSymbol, response.UNI.valueOrZero())
        "MATH" -> TokenPrice(tokenSymbol, response.MATH.valueOrZero())
        "TOMO" -> TokenPrice(tokenSymbol, response.TOMO.valueOrZero())
        "LUA" -> TokenPrice(tokenSymbol, response.LUA.valueOrZero())
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    private fun usdOrZero(response: SinglePriceResponse?): Double = response?.usdValue.valueOrZero()

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