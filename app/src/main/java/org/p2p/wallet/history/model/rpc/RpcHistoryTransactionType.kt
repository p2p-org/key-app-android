package org.p2p.wallet.history.model.rpc

enum class RpcHistoryTransactionType {
    UNKNOWN,
    SWAP,
    SEND,
    RECEIVE,
    STAKE,
    UNSTAKE,
    CREATE_ACCOUNT,
    CLOSE_ACCOUNT,
    BURN,
    MINT
}
