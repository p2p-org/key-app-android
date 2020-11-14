package com.wowlet.entities.local


data class UserSecretData(
    val secretKey: String,
    val publicKey: String,
    val seed: TweetNaclFast.Signature.KeyPair,
    val phrase: List<String>
)