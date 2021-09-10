package com.p2p.wallet.swap.api

data class RequestConfiguration(
    val commitment: String? = null,
    val encoding: String? = null,
    val dataSlice: DataSlice? = null,
    val filters: List<String>? = null,
    val limit: Int? = null,
    val before: String? = null,
    val until: String? = null
)

data class DataSlice(
    val offset: Int,
    val length: Int
)