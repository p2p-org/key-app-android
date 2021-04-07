package com.p2p.wowlet.entities.local

class BalanceInfo(
    var depositAddress: String,
    var amount: Long,
    var mint: String,
    var owner: String,
    var decimals: Int
) {
    override fun toString(): String {
        return "Address: $depositAddress amount: $amount mint: $mint owner: $owner decimals: $decimals"
    }
}