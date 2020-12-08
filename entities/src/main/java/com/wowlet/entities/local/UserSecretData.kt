package com.wowlet.entities.local

data class UserSecretData(
    var secretKey: String,
    var publicKey: String,
    var phrase: List<String>
)