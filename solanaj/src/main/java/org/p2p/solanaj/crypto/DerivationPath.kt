package org.p2p.solanaj.crypto

enum class DerivationPath(val stringValue: String) {
    BIP32DEPRECATED("m/501'/0'/0/0 (deprecated)"),
    BIP44CHANGE("m/44'/501'/0'/0' (Default)"),
    BIP44("m/44'/501'/0'");
}