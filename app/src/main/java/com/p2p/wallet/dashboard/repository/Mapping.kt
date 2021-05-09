package com.p2p.wallet.dashboard.repository

import com.p2p.wallet.dashboard.model.local.AddCoinItem
import com.p2p.wallet.dashboard.model.local.ConstWallet

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