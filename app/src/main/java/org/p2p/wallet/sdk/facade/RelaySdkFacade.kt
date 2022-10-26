package org.p2p.wallet.sdk.facade

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.sdk.RelaySdk
import org.p2p.wallet.sdk.facade.mapper.SdkMethodResultMapper
import org.p2p.wallet.sdk.facade.model.relay.RelaySignTransactionResponse
import kotlinx.coroutines.withContext

class RelaySdkFacade(
    private val relaySdk: RelaySdk,
    private val logger: AppSdkLogger,
    private val methodResultMapper: SdkMethodResultMapper,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun signTransaction(
        transaction: String,
        keypair: String,
        blockhash: String
    ): String = withContext(dispatchers.io) {
        logger.logRequest(
            "signTransaction",
            transaction,
            keypair,
            blockhash
        )

        val response = relaySdk.signTransaction(
            transaction = transaction,
            keypair = keypair,
            blockhash = blockhash,
        )
        logger.logResponse("signTransaction", response)

        methodResultMapper.fromSdk<RelaySignTransactionResponse>(response).transaction
    }
}
