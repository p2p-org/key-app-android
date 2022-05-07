package org.p2p.solanaj.kits.transaction

enum class TransactionDetailsType(val typeStr: String?) {
    SWAP("swap"),
    TRANSFER("transfer"),
    TRANSFER_CHECKED("transferChecked"),
    CREATE_ACCOUNT("create"),
    BURN_CHECKED("burnChecked"),
    CLOSE_ACCOUNT("closeAccount"),
    UNKNOWN("unknown");

    companion object {
        fun valueOf(typeStr: String?): TransactionDetailsType =
            values().firstOrNull { it.typeStr == typeStr } ?: UNKNOWN
    }
}
