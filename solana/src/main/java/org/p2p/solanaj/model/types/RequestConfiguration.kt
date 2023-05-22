package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

/**
 * Options listed here https://docs.solana.com/api/http
 */
data class RequestConfiguration(
    @SerializedName("commitment")
    val commitment: String? = null,
    @SerializedName("preflightCommitment")
    val preflightCommitment: String? = null,
    @SerializedName("encoding")
    val encoding: String? = null,
    @SerializedName("dataSlice")
    val dataSlice: DataSlice? = null,
    @SerializedName("filters")
    val filters: List<Any>? = null,
    @SerializedName("limit")
    val limit: Int? = null,
    @SerializedName("before")
    val before: String? = null,
    @SerializedName("until")
    val until: String? = null
)

data class DataSlice(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("length")
    val length: Int
)
