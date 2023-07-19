package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName

data class TokenListResponse(
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("tokens")
    val tokens: List<TokenResponse>
)

data class TokenResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("logoURI")
    val logoUrl: String?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("extensions")
    val extensions: MetadataExtensionResponse?
) {

    companion object {
        private const val WRAPPED_TAG = "wrapped-sollet"
    }

    fun isWrapped() = !tags.isNullOrEmpty() && tags.any { it == WRAPPED_TAG }
}

data class MetadataExtensionResponse(
    @SerializedName("ruleOfProcessingTokenPriceWs")
    val ruleOfProcessingTokenPriceWs: String?,
    @SerializedName("isPositionOnWs")
    val isPositionOnWs: Boolean?,
    @SerializedName("isTokenCellVisibleOnWs")
    val isTokenCellVisibleOnWs: Boolean?,
    @SerializedName("percentDifferenceToShowByPriceOnWs")
    val percentDifferenceToShowByPriceOnWs: Int?,
    @SerializedName("calculationOfFinalBalanceOnWs")
    val calculationOfFinalBalanceOnWs: Boolean?,
    @SerializedName("ruleOfFractionalPartOnWs")
    val ruleOfFractionalPartOnWs: String?,
    @SerializedName("canBeHidden")
    val canBeHidden: Boolean?
)
