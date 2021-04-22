package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.github.mikephil.charting.data.Entry
import com.p2p.wallet.R
import com.p2p.wallet.common.network.HistoricalPrices
import com.p2p.wallet.dashboard.model.local.ActivityItem
import com.p2p.wallet.dashboard.model.local.AddCoinItem
import com.p2p.wallet.dashboard.model.local.BalanceInfo
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.dashboard.model.local.EnterWallet
import com.p2p.wallet.dashboard.model.local.Token
import org.p2p.solanaj.rpc.types.TransferInfo
import java.math.BigDecimal
import kotlin.math.pow

fun BalanceInfo.walletToWallet(walletsList: List<ConstWallet>): Token {
    var walletItem = Token(
        "", "", 0, "",
        "", "", BigDecimal.ZERO, BigDecimal.ZERO, 0.0, R.color.chartSOL
    )
    walletsList.forEach {
        if (it.mint == mint) {
            if (it.tokenSymbol == "USDT" || it.tokenSymbol == "USDC") {
                walletItem = Token(
                    tokenSymbol = it.tokenSymbol,
                    mintAddress = it.mint,
                    tokenName = it.tokenName,
                    depositAddress = depositAddress,
                    decimals = decimals,
                    iconUrl = it.icon,
                    price = BigDecimal(amount.toDouble() / (10.0.pow(decimals))),
                    total = BigDecimal(amount.toDouble() / (10.0.pow(decimals))),
                    walletBinds = 1.0,
                    color = it.color
                )
            } else {
                walletItem = Token(
                    tokenSymbol = it.tokenSymbol,
                    mintAddress = it.mint,
                    tokenName = it.tokenName,
                    depositAddress = depositAddress,
                    decimals = decimals,
                    iconUrl = it.icon,
                    total = BigDecimal(amount.toDouble() / (10.0.pow(decimals))),
                    price = BigDecimal.ZERO,
                    walletBinds = 0.0,
                    color = it.color
                )
            }
        }
    }
    return walletItem
}

fun ConstWallet.fromConstWalletToAddCoinItem(
    change24h: Double,
    change24hInPercentages: Double,
    currency: Double
): AddCoinItem {
    return AddCoinItem(
        tokenName = tokenSymbol,
        mintAddress = mint,
        tokenSymbol = tokenSymbol,
        icon = icon,
        change24hPrice = change24h,
        change24hPercentages = change24hInPercentages,
        currency = currency
    )
}

fun Token.walletItemToQrCode(qrCode: Bitmap): EnterWallet {
    return EnterWallet(
        qrCode = qrCode,
        walletAddress = depositAddress,
        icon = iconUrl,
        name = tokenName
    )
}

fun HistoricalPrices.fromHistoricalPricesToChartItem(index: Int): Entry {
    return Entry(index.toFloat(), close.toFloat())
}

fun TransferInfo.transferInfoToActivityItem(
    publicKey: String,
    icon: String,
    tokenName: String,
    tokenSymbol: String,
    isCreating: Boolean = false
): ActivityItem {
    val symbolPrice: String
    val sendOrReceive = when {
        isCreating -> {
            symbolPrice = "+"
            "Create account"
        }
        from == publicKey -> {
            symbolPrice = "-"
            "Send Coin"
        }
        else -> {
            symbolPrice = "+"
            "Receive Coin"
        }
    }
    return ActivityItem(
        icon = icon,
        name = sendOrReceive,
        symbolsPrice = symbolPrice,
        price = lamports.toDouble() / (10.0.pow(9)),
        lamports = lamports.toDouble() / (10.0.pow(9)),
        slot = slot,
        signature = signature,
        fee = fee,
        from = from,
        to = to,
        tokenName = tokenName,
        tokenSymbol = tokenSymbol,
        date = ""
    )
}