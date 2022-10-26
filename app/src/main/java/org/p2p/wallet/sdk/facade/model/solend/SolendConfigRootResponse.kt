package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

data class SolendConfigRootResponse(
    @SerializedName("config")
    val solendConfig: SolendConfigResponse
)

data class SolendConfigResponse(
    @SerializedName("assets")
    val assets: List<SolendAssetResponse>,
    @SerializedName("markets")
    val markets: List<SolendMarketResponse>,
    @SerializedName("oracles")
    val oracles: SolendOraclesResponse,
    @SerializedName("programID")
    val programId: String
) {
    data class SolendAssetResponse(
        @SerializedName("decimals")
        val decimals: Int,
        @SerializedName("mintAddress")
        val mintAddress: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("symbol")
        val symbol: String
    )

    data class SolendMarketResponse(
        @SerializedName("address")
        val address: String,
        @SerializedName("authorityAddress")
        val authorityAddress: String,
        @SerializedName("isPrimary")
        val isPrimary: Boolean,
        @SerializedName("name")
        val name: String,
        @SerializedName("reserves")
        val reserves: List<SolendMarketReserveResponse>
    ) {
        data class SolendMarketReserveResponse(
            @SerializedName("address")
            val address: String,
            @SerializedName("asset")
            val asset: String,
            @SerializedName("collateralMintAddress")
            val collateralMintAddress: String,
            @SerializedName("collateralSupplyAddress")
            val collateralSupplyAddress: String,
            @SerializedName("liquidityAddress")
            val liquidityAddress: String,
            @SerializedName("liquidityFeeReceiverAddress")
            val liquidityFeeReceiverAddress: String,
            @SerializedName("userSupplyCap")
            val userSupplyCap: Int?
        )
    }

    data class SolendOraclesResponse(
        @SerializedName("assets")
        val assets: List<SolendOracleAssetResponse>,
        @SerializedName("pythProgramID")
        val pythProgramID: String,
        @SerializedName("switchboardProgramID")
        val switchboardProgramID: String
    ) {
        data class SolendOracleAssetResponse(
            @SerializedName("asset")
            val asset: String,
            @SerializedName("priceAddress")
            val priceAddress: String,
            @SerializedName("switchboardFeedAddress")
            val switchboardFeedAddress: String
        )
    }
}
