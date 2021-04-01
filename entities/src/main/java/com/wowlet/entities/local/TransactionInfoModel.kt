package com.wowlet.entities.local

data class TransactionInfoModel(
    val slot: Long,
    val signature: String,
    val lamport: Long,
    val from: String,
    val to: String
)