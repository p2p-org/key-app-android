package org.p2p.wallet.striga.wallet.models

enum class StrigaOnchainTxType {
    ON_CHAIN_WITHDRAWAL_INITIATED,
    ON_CHAIN_WITHDRAWAL_DENIED,
    ON_CHAIN_WITHDRAWAL_PENDING,
    ON_CHAIN_WITHDRAWAL_CONFIRMED,
    ON_CHAIN_WITHDRAWAL_FAILED;

    companion object {
        fun from(name: String): StrigaOnchainTxType {
            return values().firstOrNull {
                it.name.equals(name, ignoreCase = true)
            } ?: error("Unknown onchain tx type: $name")
        }
    }
}
