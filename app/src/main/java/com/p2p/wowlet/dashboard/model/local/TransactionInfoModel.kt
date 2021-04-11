package com.p2p.wowlet.dashboard.model.local

data class TransactionInfoModel(
    val slot: Long,
    val signature: String,
    val lamport: Long,
    val from: String,
    val to: String
)