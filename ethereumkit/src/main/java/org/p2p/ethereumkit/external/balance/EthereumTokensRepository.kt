package org.p2p.ethereumkit.external.balance

import com.google.gson.Gson
import org.p2p.ethereumkit.external.api.alchemy.AlchemyService
import org.p2p.ethereumkit.external.core.EthereumNetworkEnvironment
import org.p2p.ethereumkit.external.api.alchemy.request.GetBalanceJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.request.GetTokenBalancesJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.request.GetTokenMetadataJsonRpc
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalancesResponse
import org.p2p.ethereumkit.external.api.alchemy.response.TokenMetadataResponse
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import java.math.BigInteger
import java.net.URI

internal class EthereumTokensRepository(
    private val alchemyService: AlchemyService,
    private val networkEnvironment: EthereumNetworkEnvironment,
    private val gson: Gson
) : TokensRepository {

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
        return request.parseResponse(response, gson)
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
