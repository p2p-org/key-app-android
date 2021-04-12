package com.p2p.wallet.common.network

data class CallException<ErrorBody>(
    val errorCode: Int,
    val errorMessage: String? = null,
    val errorBody: ErrorBody? = null
)