package com.wowlet.entities.local

data class CoinItem(
    val name: String,
    var priceInUs: String = "",
    var priceInBTC: String = "",
    var type: String = ""
)