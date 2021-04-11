package com.p2p.wowlet.dashboard.model.local

data class CoinItem(
    val name: String,
    var priceInUs: String = "",
    var priceInBTC: String = "",
    var type: String = ""
)