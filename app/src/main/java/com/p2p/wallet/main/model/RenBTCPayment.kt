package com.p2p.wallet.main.model

data class RenBTCPayment(
    val transactionHash: String,
    val txIndex: Int,
    val amount: Long
)