package org.p2p.wallet.history.model

enum class HistoryTransactionType(val typeStr: String) {
    UNKNOWN("unknown"),
    SWAP("swap"),
    TRANSFER("transfer"),
    CREATE_ACCOUNT("create"),
    CLOSE_ACCOUNT("closeAccount")
}
