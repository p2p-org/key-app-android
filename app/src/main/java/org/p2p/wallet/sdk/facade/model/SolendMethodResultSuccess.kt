package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal
import java.math.BigInteger

sealed interface SolendMethodResultSuccess {
    class SolendDepositTransactions(
        val transactions: List<String>
    ) : SolendMethodResultSuccess

    class SolendWithdrawTransactions(
        val transactions: List<String>
    ) : SolendMethodResultSuccess

    class SolendCollateralAccountsList(
        @SerializedName("accounts")
        val accounts: List<SolendCollateralAccount>
    ) : SolendMethodResultSuccess

    class SolendCollateralAccount(
        @SerializedName("address")
        val accountAddress: Base58String,
        @SerializedName("mint")
        val accountMint: Base58String
    ) : SolendMethodResultSuccess

    class SolendTokenDepositFees(
        @SerializedName("fee")
        val accountCreationFee: Long,
        @SerializedName("rent")
        val rent: Long
    ) : SolendMethodResultSuccess

    class SolendMarketInformation(
        @SerializedName("token_symbol")
        val tokenSymbol: String,
        @SerializedName("current_supply")
        val currentSupply: BigDecimal,
        @SerializedName("deposit_limit")
        val depositLimitAmount: BigInteger,
        @SerializedName("supply_interest")
        val supplyInterest: BigDecimal
    ) : SolendMethodResultSuccess

    class SolendUserDepositByTokenResponse(
        @SerializedName("market_info")
        val userDepositBySymbol: SolendUserDeposit
    ) : SolendMethodResultSuccess

    class SolendUserDepositsResponse(
        @SerializedName("market_info")
        val deposits: List<SolendUserDeposit>
    ) : SolendMethodResultSuccess

    class SolendUserDeposit(
        @SerializedName("depositedAmount")
        val depositedAmount: BigDecimal,
        @SerializedName("symbol")
        val depositTokenSymbol: String
    ) : SolendMethodResultSuccess
}
