package org.p2p.wallet.solend.model

import org.p2p.wallet.user.model.TokenData

class SolendDomainMapper {

    fun toDepositToken(
        tokenData: TokenData,
        marketInfo: SolendMarketInfo,
        activeDeposit: SolendUserDeposit?
    ): SolendDepositToken {
        return if (activeDeposit != null) {
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
    }
}
