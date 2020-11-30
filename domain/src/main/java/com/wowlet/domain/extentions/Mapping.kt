package com.wowlet.domain.extentions

import com.wowlet.entities.local.*
import org.p2p.solanaj.rpc.types.TransferInfo

fun ConstWalletItem.constWalletToWallet(walletsList: List<BalanceInfo>): WalletItem {
    var walletItem = WalletItem()
    walletsList.forEach {
        if (it.mint == mintAddress) {
            if (tokenSymbol == "USDT" || tokenSymbol == "USDC") {
                walletItem = WalletItem(
                    tokenSymbol = tokenSymbol,
                    mintAddress = mintAddress,
                    tokenName = tokenName,
                    depositAddress = it.depositAddress,
                    decimals = it.decimals,
                    icon = icon,
                    price = it.amount.toDouble(),
                    tkns = it.amount.toDouble()
                )
            } else {
                walletItem = WalletItem(
                    tokenSymbol = tokenSymbol,
                    mintAddress = mintAddress,
                    tokenName = tokenName,
                    depositAddress = it.depositAddress,
                    decimals = it.decimals,
                    icon = icon,
                    tkns = it.amount.toDouble()
                )
            }
        }
    }
    return walletItem
}

fun ConstWalletItem.fromConstWalletToAddCoinItem(
    change24h: Double,
    change24hInPercentages: Double
): AddCoinItem {
    return AddCoinItem(
        tokenName = tokenSymbol,
        mintAddress = mintAddress,
        tokenSymbol = tokenSymbol,
        icon = icon,
        change24hPrice = change24h,
        change24hPercentages = change24hInPercentages
    )
}

fun TransferInfo.transferInfoToActivityItem(publicKey: String, icon: String): ActivityItem {
    val symbolPrice: String
    val sendOrReceive = if (this.from == publicKey) {
        symbolPrice = "-"
        "Send Coin"

    } else {
        symbolPrice = "+"
        "Receive Coin"
    }
    return ActivityItem(
        icon = icon,
        name = sendOrReceive,
        symbolsPrice = symbolPrice,
        price = lamports.toDouble(),
        tknsValue = lamports.toDouble(),
        slot = slot,
        signature = signature,
        fee=fee,
        from=from,
        to=to
    )
}

