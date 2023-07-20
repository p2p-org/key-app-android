package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

data class StrigaAccountStatementResponse(
    @SerializedName("transactions")
    val transactions: List<AccountTransactionResponse>,
    @SerializedName("count")
    val pagingCount: Int,
    @SerializedName("total")
    val total: Int
) {
    data class AccountTransactionResponse(
        @SerializedName("id")
        val id: String,
        @SerializedName("syncedOwnerId")
        val syncedOwnerId: String,
        @SerializedName("credit")
        val credit: Long,
        @SerializedName("debit")
        val debit: Long,
        @SerializedName("timestamp")
        val timestamp: String, // // 2022-08-29T10:09:25.907Z
        @SerializedName("txType")
        val txType: String,
        @SerializedName("nonFpFeeBaseCurrency")
        val nonFpFeeBaseCurrency: Long,
        @SerializedName("feeEur")
        val feeEur: Long,
        @SerializedName("exchangeRate")
        val exchangeRate: Long,
        @SerializedName("balanceBefore")
        val balanceBefore: AccountBalanceResponse,
        @SerializedName("balanceAfter")
        val balanceAfter: AccountBalanceResponse,
        @SerializedName("bankingTransactionShortId")
        val bankingTransactionShortId: String?,
        @SerializedName("bankingPaymentType")
        val bankingPaymentType: String?,
        @SerializedName("bankingTransactionDateTime")
        val bankingTransactionDateTime: String?, // 2022-08-29T10:09:25.421Z
        @SerializedName("bankingTransactionReference")
        val bankingTransactionReference: String?,
        @SerializedName("bankingSenderBic")
        val bankingSenderBic: String?,
        @SerializedName("bankingSenderIban")
        val bankingSenderIban: String?,
        @SerializedName("bankingSenderName")
        val bankingSenderName: String?,
        @SerializedName("bankingBeneficiaryBic")
        val bankingBeneficiaryBic: String?,
        @SerializedName("bankingBeneficiaryIban")
        val bankingBeneficiaryIban: String?,
        @SerializedName("bankingTransactionId")
        val bankingTransactionId: String?,
        @SerializedName("bankingSenderInformation")
        val bankingSenderInformation: String?,
        @SerializedName("bankingSenderRoutingCodes")
        val bankingSenderRoutingCodes: List<String>?,
        @SerializedName("bankingSenderAccountNumber")
        val bankingSenderAccountNumber: String?
    ) {
        data class AccountBalanceResponse(
            @SerializedName("amount")
            val amount: Long,
            @SerializedName("currency")
            val currencyUnits: String
        )
    }
}
