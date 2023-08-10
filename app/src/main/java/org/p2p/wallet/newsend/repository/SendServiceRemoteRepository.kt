package org.p2p.wallet.newsend.repository

import com.google.gson.Gson
import java.math.BigInteger
import java.net.URI
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.newsend.api.GenerateSendTransactionRequest
import org.p2p.wallet.newsend.api.GenerateSendTransactionResponse

class SendServiceRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    private val urlProvider: NetworkServicesUrlProvider
) : SendServiceRepository {

    private val sendServiceStringUrl: URI
        get() = urlProvider.loadSendServiceEnvironment().baseServiceUrl.toURI()

    override suspend fun generateTransaction(
        userPublicKey: Base58String,
        tokenMint: Base58String,
        amountInLamports: BigInteger,
        recipientAddress: Base58String
    ): GenerateSendTransactionResponse {
        val request = GenerateSendTransactionRequest(
            userWallet = userPublicKey,
            mintAddress = tokenMint,
            amount = amountInLamports.toString(),
            recipient = recipientAddress
        )

        val requestJson = gson.toJson(request)
        val response = api.launch(sendServiceStringUrl, requestJson)
        return request.parseResponse(response, gson)
    }

    private fun String.toURI(): URI = URI(this)
}
