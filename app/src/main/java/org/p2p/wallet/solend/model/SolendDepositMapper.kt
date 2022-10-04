package org.p2p.wallet.solend.model

import org.p2p.wallet.sdk.facade.model.SolendMarketInformationResponse
import org.p2p.wallet.sdk.facade.model.SolendUserDepositResponse
import org.p2p.wallet.user.model.TokenData

class SolendDepositMapper {

    fun fromNetwork(
        tokenData: TokenData,
        marketInfo: SolendMarketInfo,
        activeDeposit: SolendUserDeposit?
    ): SolendDepositToken = if (activeDeposit != null) {
        SolendDepositToken.Active(
            tokenName = tokenData.name,
            tokenSymbol = tokenData.symbol,
            iconUrl = tokenData.iconUrl,
            supplyInterest = marketInfo.supplyInterest,
            depositAmount = activeDeposit.depositedAmount
        )
    } else {
        SolendDepositToken.Inactive(
            tokenName = tokenData.name,
            tokenSymbol = tokenData.symbol,
            iconUrl = tokenData.iconUrl,
            supplyInterest = marketInfo.supplyInterest
        )
    }

    fun fromNetwork(response: SolendUserDepositResponse): SolendUserDeposit =
        SolendUserDeposit(
            depositedAmount = response.depositedAmount,
            depositTokenSymbol = response.depositTokenSymbol
        )

    fun fromNetwork(response: SolendMarketInformationResponse): List<SolendMarketInfo> =
        response.marketInfo
            .map { it.asJsonArray }
            .map {
                val tokenSymbol = it.get(0).asString
                val marketInfoObject = it.get(1).asJsonObject
                SolendMarketInfo(
                    tokenSymbol = tokenSymbol,
                    currentSupply = marketInfoObject.get("current_supply").asBigDecimal,
                    depositLimitAmount = marketInfoObject.get("deposit_limit").asBigInteger,
                    supplyInterest = marketInfoObject.get("supply_interest").asBigDecimal,
                )
            }
}
