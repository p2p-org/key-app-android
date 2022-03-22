package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class RequestConfiguration(
    @SerializedName("commitment")
    val commitment: String? = null,
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

enum class Encoding(val encoding: String) {
    @SerializedName("base64")
    BASE64("base64");
}

data class DataSlice(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("length")
    val length: Int
)
