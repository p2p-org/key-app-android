package org.p2p.token.service.api.tokenservice.response

import com.google.gson.annotations.SerializedName

internal data class TokenListResponse(
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("tokens")
    val tokens: List<TokenResponse>
)

internal data class TokenResponse(
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

internal data class MetadataExtensionResponse(
    @SerializedName("ruleOfProcessingTokenPriceWS")
    val ruleOfProcessingTokenPriceWs: String?,
    @SerializedName("isPositionOnWS")
    val isPositionOnWs: Boolean?,
    @SerializedName("isTokenCellVisibleOnWS")
    val isTokenCellVisibleOnWs: Boolean?,
    @SerializedName("percentDifferenceToShowByPriceOnWS")
    val percentDifferenceToShowByPriceOnWs: Double?,
    @SerializedName("calculationOfFinalBalanceOnWS")
    val calculationOfFinalBalanceOnWs: Boolean?,
    @SerializedName("ruleOfFractionalPartOnWS")
    val ruleOfFractionalPartOnWs: String?,
    @SerializedName("canBeHidden")
    val canBeHidden: Boolean?
)
