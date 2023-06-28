package org.p2p.wallet.striga.wallet.models

enum class StrigaNetworkType {
    BTC,
    ETH,
    BSC;

    // TODO: it doesn't exist for now, should be added when is ready
    /* SOL */

    companion object {
        fun from(name: String): StrigaNetworkType {
            return values().firstOrNull {
                it.name.equals(name, ignoreCase = true)
            } ?: error("Unknown network type: $name")
        }
    }
}
