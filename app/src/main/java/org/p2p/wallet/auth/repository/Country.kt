package org.p2p.wallet.auth.repository

class Country(
    val name: String,
    val flagEmoji: String,
) {
    val nameLowercase: String
        get() = name.lowercase()
}
