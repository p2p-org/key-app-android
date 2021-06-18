package org.p2p.solanaj.data.api

import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ConfirmedTransaction
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcResultTypes
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenAccounts
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcApi {

    @POST
    suspend fun getTokenAccountBalance(@Body rpcRequest: RpcRequest, @Url url: String = ""): TokenAccountBalance

    @POST
    suspend fun getRecentBlockhash(@Body rpcRequest: RpcRequest, @Url url: String = ""): RecentBlockhash

    @POST
    suspend fun sendTransaction(@Body rpcRequest: RpcRequest, @Url url: String = ""): String

    @POST
    suspend fun getAccountInfo(@Body rpcRequest: RpcRequest, @Url url: String = ""): AccountInfo

    @POST
    suspend fun getProgramAccounts(@Body rpcRequest: RpcRequest, @Url url: String = ""): List<ProgramAccount>

    @POST
    suspend fun getBalance(@Body rpcRequest: RpcRequest, @Url url: String = ""): RpcResultTypes.ValueLong

    @POST
    suspend fun getTokenAccountsByOwner(@Body rpcRequest: RpcRequest, @Url url: String = ""): TokenAccounts

    @POST
    suspend fun getConfirmedSignaturesForAddress2(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): List<SignatureInformation>

    @POST
    suspend fun getConfirmedTransaction(@Body rpcRequest: RpcRequest, @Url url: String = ""): ConfirmedTransaction

    @POST
    suspend fun getBlockTime(@Body rpcRequest: RpcRequest, @Url url: String = ""): Long

    @POST
    suspend fun getMinimumBalanceForRentExemption(@Body rpcRequest: RpcRequest, @Url url: String = ""): Long

    @POST
    suspend fun getMultipleAccounts(@Body rpcRequest: RpcRequest, @Url url: String = ""): MultipleAccountsInfo
}