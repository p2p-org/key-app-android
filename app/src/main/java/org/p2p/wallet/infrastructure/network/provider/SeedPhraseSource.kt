package org.p2p.wallet.infrastructure.network.provider

enum class SeedPhraseSource(val title: String) {
    MANUAL("Manual"),
    WEB_AUTH("Web3Auth"),
    NOT_PROVIDED("");

    companion object {
        fun getValueOf(value: String?): SeedPhraseSource = values().firstOrNull {
            it.title == value
        } ?: NOT_PROVIDED
    }
}
