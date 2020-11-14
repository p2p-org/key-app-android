package com.wowlet.entities.local

class BalanceInfo (var depositAddress: String, var amount: Long) {
    override fun toString(): String {
        return "Address: $depositAddress amount: $amount"
    }
}