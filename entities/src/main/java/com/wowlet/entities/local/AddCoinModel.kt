package com.wowlet.entities.local

data class AddCoinModel(
    var minimumBalance: Int,
    var addCoinList: MutableList<AddCoinItem>
)