package org.p2p.wallet.rpc.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.core.network.data.CommonResponse
import org.p2p.solanaj.kits.AccountInfoParsed
import org.p2p.solanaj.kits.MultipleAccountsInfo
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccounts
import org.p2p.wallet.utils.emptyString

interface RpcAccountApi {
    @POST
    suspend fun getAccountInfoParsed(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<AccountInfoParsed.RootValue>

    @POST
    suspend fun getAccountInfo(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<AccountInfo>

    @POST
    suspend fun getAccountsInfo(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = emptyString()
    ): List<CommonResponse<AccountInfo>>

    @POST
    suspend fun getProgramAccounts(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<ProgramAccount>>

    @POST
    suspend fun getTokenAccountsByOwner(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<TokenAccounts>

    @POST
    suspend fun getMultipleAccounts(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<MultipleAccountsInfo>
}
