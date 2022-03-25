package org.p2p.solanaj.kits.transaction

enum class TransactionDetailsType(val typeStr: String) {
    UNKNOWN("unknown"),
    SWAP("swap"),
    TRANSFER("transfer"),
    CREATE_ACCOUNT("create"),
    CLOSE_ACCOUNT("closeAccount");
}
