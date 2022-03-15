package org.p2p.wallet.rpc.api

import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcAccountApi {
    @POST
    suspend fun getAccountInfo(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<AccountInfo>

    @POST
    suspend fun getAccountsInfo(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = ""
    ): List<CommonResponse<AccountInfo>>

    @POST
    suspend fun getProgramAccounts(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<List<ProgramAccount>>

    @POST
    suspend fun getTokenAccountsByOwner(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<TokenAccounts>

    @POST
    suspend fun getMultipleAccounts(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<MultipleAccountsInfo>
}