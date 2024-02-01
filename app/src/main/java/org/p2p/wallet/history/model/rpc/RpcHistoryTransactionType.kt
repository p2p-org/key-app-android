package org.p2p.wallet.history.model.rpc

enum class RpcHistoryTransactionType {
    UNKNOWN,
    SWAP,
    SEND,
    RECEIVE,
    REFERRAL_REWARD,
    STAKE,
    UNSTAKE,
    CREATE_ACCOUNT,
    CLOSE_ACCOUNT,
    BURN,
    MINT,
    WORMHOLE_RECEIVE,
    WORMHOLE_SEND
}
