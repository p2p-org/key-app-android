package org.p2p.wallet.striga.wallet.models

enum class StrigaOnchainTxStatus {
    PENDING_2FA_CONFIRMATION,

    UNKNOWN;

    companion object {
        fun from(name: String): StrigaOnchainTxStatus {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
