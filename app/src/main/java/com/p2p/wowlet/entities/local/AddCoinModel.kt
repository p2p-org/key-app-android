package com.p2p.wowlet.entities.local

data class AddCoinModel(
    var minimumBalance: Long,
    var addCoinList: MutableList<AddCoinItem>
)