package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

sealed class RpcHistoryTransactionInfoResponse {

    data class Send(
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse,
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("account")
        val counterParty: RpcHistoryAccountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class Receive(
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse,
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("account")
        val counterParty: RpcHistoryAccountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class Swap(
        @SerializedName("from")
        val from: RpcHistorySwapTokenResponse,
        @SerializedName("to")
        val to: RpcHistorySwapTokenResponse,
        @SerializedName("transitive")
        val transitive: List<RpcHistorySwapTokenResponse>?,
        @SerializedName("swap_programs")
        val swapPrograms: List<RpcHistoryAccountResponse>?
    ) : RpcHistoryTransactionInfoResponse()

    data class Stake(
        @SerializedName("stake")
        val account: RpcHistoryAccountResponse,
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class Unstake(
        @SerializedName("stake")
        val account: RpcHistoryAccountResponse,
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class CreateAccount(
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class CloseAccount(
        @SerializedName("token")
        val token: RpcHistoryTokenResponse?,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse?
    ) : RpcHistoryTransactionInfoResponse()

    data class Burn(
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class Mint(
        @SerializedName("token")
        val token: RpcHistoryTokenResponse,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse
    ) : RpcHistoryTransactionInfoResponse()

    data class Unknown(
        @SerializedName("token")
        val token: RpcHistoryTokenResponse?,
        @SerializedName("amount")
        val amount: RpcHistoryAmountResponse?
    ) : RpcHistoryTransactionInfoResponse()
}
