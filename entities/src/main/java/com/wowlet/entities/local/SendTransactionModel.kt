package com.wowlet.entities.local

data class SendTransactionModel(
    val toPublickKey: String,
    val lamports: Int
)