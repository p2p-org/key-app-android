package org.p2p.wallet.solend.model

import org.p2p.core.token.Token
import org.p2p.wallet.sdk.facade.model.solend.SolendMarketInformationResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendUserDepositResponse
import org.p2p.core.token.TokenData
import org.p2p.core.utils.orZero
import org.p2p.token.service.model.TokenServicePrice

class SolendDepositMapper {

    fun fromNetwork(
        tokenData: TokenData,
        tokenPrice: TokenServicePrice?,
        userToken: Token.Active?,
        marketInfo: SolendMarketInfo?,
        activeDeposit: SolendUserDeposit?
    ): SolendDepositToken {
        val usdRate = tokenPrice?.price.orZero()
        return if (activeDeposit != null) {
            SolendDepositToken.Active(
                tokenName = tokenData.name,
                tokenSymbol = tokenData.symbol,
                decimals = tokenData.decimals,
                usdRate = usdRate,
                mintAddress = tokenData.mintAddress,
                iconUrl = tokenData.iconUrl,
                supplyInterest = marketInfo?.supplyInterest,
                depositAmount = activeDeposit.depositedAmount,
                usdAmount = activeDeposit.depositedAmount * usdRate,
                availableTokensForDeposit = userToken?.total.orZero()
            )
        } else {
            SolendDepositToken.Inactive(
                tokenName = tokenData.name,
                tokenSymbol = tokenData.symbol,
                decimals = tokenData.decimals,
                mintAddress = tokenData.mintAddress,
                usdRate = usdRate,
                iconUrl = tokenData.iconUrl,
                supplyInterest = marketInfo?.supplyInterest,
                availableTokensForDeposit = userToken?.total.orZero()
            )
        }
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
