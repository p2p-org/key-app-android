package org.p2p.solanaj.crypto

enum class DerivationPath(val stringValue: String) {
    BIP32DEPRECATED("m/501'/0'/0/0 (deprecated)"),
    BIP44CHANGE("m/44'/501'/0'/0' (Default)"),
    BIP44("m/44'/501'/0'");

    companion object {
        fun parse(path: String): DerivationPath = when (path) {
            BIP44.stringValue -> BIP44
            BIP44CHANGE.stringValue -> BIP44CHANGE
            else -> BIP32DEPRECATED
        }
    }
}