package com.p2p.wallet.dashboard.model.local

data class UserSecretData(
    val secretKey: String,
    val publicKey: String,
    val keys: List<String>
)