package com.p2p.wowlet.entities.local

import com.github.mikephil.charting.data.PieEntry

data class YourWallets(
    val wallets: List<WalletItem>,
    val balance: Double,
    val mainWallets: List<WalletItem>,
    val pieChartList: List<PieEntry>
)