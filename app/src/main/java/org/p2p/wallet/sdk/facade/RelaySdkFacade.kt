package org.p2p.wallet.sdk.facade

import kotlinx.coroutines.withContext
import org.p2p.solanaj.model.types.RecentBlockhashResponse
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.encodeToBase58
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.sdk.RelaySdk
import org.p2p.wallet.sdk.facade.mapper.SdkMethodResultMapper
import org.p2p.wallet.sdk.facade.model.relay.RelaySdkSignedTransaction
import org.p2p.wallet.sdk.facade.model.relay.RelaySignTransactionResponse
import org.p2p.wallet.utils.Base58String

class RelaySdkFacade(
    private val relaySdk: RelaySdk,
    private val logger: AppSdkLogger,
    private val methodResultMapper: SdkMethodResultMapper,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun signTransaction(
        transaction: Base58String,
        keyPair: Base58String,
        recentBlockhash: RecentBlockhashResponse?
    ): RelaySdkSignedTransaction {
        return actualSignTransaction(
            transaction = transaction.base58Value,
            keyPair = keyPair.decodeToBytes(),
            recentBlockhash = recentBlockhash?.recentBlockhash.orEmpty()
        )
    }

    suspend fun signTransaction(
        transaction: Base64String,
        keyPair: Base58String,
        recentBlockhash: RecentBlockhashResponse?
    ): RelaySdkSignedTransaction = actualSignTransaction(
        transaction = transaction.base64Value,
        keyPair = keyPair.decodeToBytes(),
        recentBlockhash = recentBlockhash?.recentBlockhash.orEmpty()
    )

    private suspend fun actualSignTransaction(
        transaction: String,
        keyPair: ByteArray,
        recentBlockhash: String
    ) = withContext(dispatchers.io) {
        logger.logRequest(methodName = "signTransaction", transaction, keyPair.size, recentBlockhash)

        check(keyPair.size == 64) {
            "keyPair for SDK should be exact 64 bytes (32 private + 32 public); now = ${keyPair.size}"
        }

        val response = relaySdk.signTransaction(
            transaction = transaction,
            keypair = keyPair.encodeToBase58(),
            blockhash = recentBlockhash,
        )
        logger.logResponse("signTransaction", response)

        RelaySdkSignedTransaction(
            methodResultMapper.fromSdk<RelaySignTransactionResponse>(response).transactionAsBase58
        )
    }
}
