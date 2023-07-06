package org.p2p.wallet.striga.wallet.models

enum class StrigaFiatAccountStatus {
    ACTIVE;

    companion object {
        fun from(name: String): StrigaFiatAccountStatus {
            return values().firstOrNull {
                it.name.equals(name, ignoreCase = true)
            } ?: error("Unknown account status: $name")
        }
    }
}
