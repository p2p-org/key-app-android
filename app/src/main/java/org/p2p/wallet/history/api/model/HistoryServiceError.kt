package org.p2p.wallet.history.api.model

enum class HistoryServiceError(val errorCode: Int) {

    INVALID_JSON(-32700),
    INVALID_REQUEST(-32600),
    METHOD_NOT_FOUND(-32601),
    INVALID_PARAMS(-32602),
    INTERNAL_RPC_ERROR(-32603),
    DB_QUERY_ERROR(-32001),
    DB_CONNECTION_ERROR(32002),
    CONNECTION_FAILED(-32010),
    CONNECTION_TIMEOUT(-32011)
}
