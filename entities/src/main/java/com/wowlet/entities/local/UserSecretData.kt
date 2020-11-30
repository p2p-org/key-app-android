package com.wowlet.entities.local

import org.p2p.solanaj.utils.TweetNaclFast


data class UserSecretData(
    var secretKey: String,
    var publicKey: String,
    var seed: TweetNaclFast.Signature.KeyPair,
    var phrase: List<String>
)