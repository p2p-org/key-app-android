package org.p2p.wallet.newsend.repository

import com.google.gson.Gson
import java.math.BigInteger
import java.net.URI
import org.p2p.core.crypto.Base64String
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.newsend.api.GenerateSendTransactionRequest
import org.p2p.wallet.newsend.api.GenerateTransactionResponse

class SendServiceRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider
) : SendServiceRepository {

    private val sendServiceStringUrl = urlProvider.loadSendServiceEnvironment().baseServiceUrl.toURI()

    override suspend fun generateTransaction(
        userPublicKey: Base64String,
        tokenMint: Base64String,
        amountInLamports: BigInteger,
        recipientAddress: String
    ): GenerateTransactionResponse {
        val request = GenerateSendTransactionRequest(
            userWallet = userPublicKey.base64Value,
            mintAddress = tokenMint.base64Value,
            amount = amountInLamports.toString(),
            recipient = recipientAddress
        )

        val requestJson = gson.toJson(request)
        val response = api.launch(sendServiceStringUrl, requestJson)
        return request.parseResponse(response, gson)
    }

    private fun String.toURI(): URI = URI(this)
}
