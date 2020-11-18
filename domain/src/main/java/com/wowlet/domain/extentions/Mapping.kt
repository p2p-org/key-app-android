package com.wowlet.domain.extentions
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.local.WalletItem
fun ConstWalletItem.constWalletToWallet(walletsList: List<BalanceInfo>): WalletItem {
    var walletItem = WalletItem()
    walletsList.forEach {
        if (it.mint == mintAddress) {
            if (tokenSymbol == "USDT") {
                walletItem = WalletItem(
                    tokenSymbol = tokenSymbol,
                    mintAddress = mintAddress,
                    tokenName = tokenName,
                    icon = icon,
                    price=it.amount.toDouble(),
                    tkns = it.amount.toDouble()
                )
            } else {
                walletItem = WalletItem(
                    tokenSymbol = tokenSymbol,
                    mintAddress = mintAddress,
                    tokenName = tokenName,
                    icon = icon,
                    tkns = it.amount.toDouble()
                )
            }
        }
    }
    return walletItem
}

