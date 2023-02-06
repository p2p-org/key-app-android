package org.p2p.wallet.history.model

data class NewHistoryTransaction(
    val signature: String,
    val type: NewHistoryTransactionType,

)

enum class NewHistoryTransactionType {
    Send,
    Receive,
    Swap,
    Unknown,
    CreateAccount
}
