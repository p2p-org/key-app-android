package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName

data class SolendConfigResponse(
    @SerializedName("config")
    val solendConfig: SolendConfig
)

data class SolendConfig(
    @SerializedName("assets")
    val assets: List<SolendAsset>,
    @SerializedName("markets")
    val markets: List<SolendMarket>,
    @SerializedName("oracles")
    val oracles: SolendOracles,
    @SerializedName("programID")
    val programId: String
) {
    data class SolendAsset(
        @SerializedName("decimals")
        val decimals: Int,
        @SerializedName("mintAddress")
        val mintAddress: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("symbol")
        val symbol: String
    )

    data class SolendMarket(
        @SerializedName("address")
        val address: String,
        @SerializedName("authorityAddress")
        val authorityAddress: String,
        @SerializedName("isPrimary")
        val isPrimary: Boolean,
        @SerializedName("name")
        val name: String,
        @SerializedName("reserves")
        val reserves: List<SolendMarketReserve>
    ) {
        data class SolendMarketReserve(
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

    data class SolendOracles(
        @SerializedName("assets")
        val assets: List<SolendOracleAsset>,
        @SerializedName("pythProgramID")
        val pythProgramID: String,
        @SerializedName("switchboardProgramID")
        val switchboardProgramID: String
    ) {
        data class SolendOracleAsset(
            @SerializedName("asset")
            val asset: String,
            @SerializedName("priceAddress")
            val priceAddress: String,
            @SerializedName("switchboardFeedAddress")
            val switchboardFeedAddress: String
        )
    }
}
