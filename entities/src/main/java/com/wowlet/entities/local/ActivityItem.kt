package com.wowlet.entities.local

data class ActivityItem(
    var icon: String,
    val name: String,
    val symbolsPrice: String,
    val price: Double,
    val lamports: Double,
    val slot: Long,
    val signature: String,
    val fee: Long,
    val from: String,
    val to: String,
    val tokenName: String,
    var tokenSymbol: String,
    val chartList: List<ChartListItem> = listOf(),
    var date: String,
    var currency: Double = lamports.div(price),
    var isReceive: Boolean = symbolsPrice == "+"
)