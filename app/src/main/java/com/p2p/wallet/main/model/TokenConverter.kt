package com.p2p.wallet.main.model

import com.p2p.wallet.dashboard.model.local.BalanceInfo
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.dashboard.model.local.Token
import kotlin.math.pow

object TokenConverter {

//    fun fromNetwork(item: ConstWalletItem) : Token =
//        Token(
//            tokenSymbol = item.tokenSymbol
//        )

    fun BalanceInfo.walletToWallet(walletsList: List<ConstWallet>): Token {
        var walletItem = Token(
            "", "", 0, "",
            "", "", 0.0, 0.0, 0.0
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
                        price = amount.toDouble() / (10.0.pow(decimals)),
                        amount = amount.toDouble() / (10.0.pow(decimals)),
                        walletBinds = 1.0
                    )
                } else {
                    walletItem = Token(
                        tokenSymbol = it.tokenSymbol,
                        mintAddress = it.mint,
                        tokenName = it.tokenName,
                        depositAddress = depositAddress,
                        decimals = decimals,
                        iconUrl = it.icon,
                        amount = amount.toDouble() / (10.0.pow(decimals)),
                        price = 0.0,
                        walletBinds = 0.0
                    )
                }
            }
        }
        return walletItem
    }
}