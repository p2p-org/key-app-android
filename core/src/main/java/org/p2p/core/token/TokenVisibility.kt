package org.p2p.core.token

enum class TokenVisibility(val stringValue: String) {
    SHOWN("SHOWN"),
    HIDDEN("HIDDEN"),
    DEFAULT("DEFAULT");

    companion object {
        fun parse(value: String): TokenVisibility = when (value) {
            SHOWN.stringValue -> SHOWN
            HIDDEN.stringValue -> HIDDEN
            else -> DEFAULT
        }
    }
}
