package com.p2p.wallet.dashboard.model.local

data class AddCoinModel(
    var minimumBalance: Long,
    var addCoinList: MutableList<AddCoinItem>
)