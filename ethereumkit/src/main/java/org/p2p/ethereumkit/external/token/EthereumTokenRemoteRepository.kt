package org.p2p.ethereumkit.external.token

import com.google.gson.Gson
import java.math.BigInteger
import java.net.URI
import org.p2p.core.model.DefaultBlockParameter
import org.p2p.core.rpc.RpcApi
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.core.wrapper.eth.hexStringToBigInteger
import org.p2p.ethereumkit.external.api.alchemy.request.GetBalanceJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.request.GetTokenBalancesJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.request.GetTokenMetadataJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalancesResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment

internal class EthereumTokenRemoteRepository(
    private val alchemyService: RpcApi,
    private val networkEnvironment: EthereumNetworkEnvironment,
    private val gson: Gson
) : EthereumTokenRepository {

    override suspend fun getWalletBalance(address: EthAddress): BigInteger {
        val request = GetBalanceJsonRpc(
            address = address,
            defaultBlockParameter = DefaultBlockParameter.Latest
        )
        val requestGson = gson.toJson(request)
        val response = alchemyService.launch(
            uri = URI.create(networkEnvironment.baseUrl),
            jsonRpc = requestGson
        )
        return request.parseResponse(response, gson).hexStringToBigInteger()
    }

    override suspend fun getTokenBalances(
        address: EthAddress,
        tokenAddresses: List<EthAddress>
    ): TokenBalancesResponse {
        val request = GetTokenBalancesJsonRpc(
            address = address,
            tokenAddresses = tokenAddresses
        )
        val requestGson = gson.toJson(request)
        val response = alchemyService.launch(
            uri = URI.create(networkEnvironment.baseUrl),
            jsonRpc = requestGson
        )
        return request.parseResponse(response, gson)
    }

    override suspend fun getTokenMetadata(contractAddresses: EthAddress): TokenMetadataResponse {
        val request = GetTokenMetadataJsonRpc(
            contractAddresses = contractAddresses
        )

        val requestGson = gson.toJson(request)
        val response = alchemyService.launch(
            uri = URI.create(networkEnvironment.baseUrl),
            jsonRpc = requestGson
        )

        return request.parseResponse(response, gson)
    }
}
