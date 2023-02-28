package org.p2p.wallet.history.signature

import kotlinx.coroutines.withContext
import org.p2p.wallet.auth.gateway.repository.mapper.BorshSerializable
import org.p2p.wallet.auth.gateway.repository.mapper.PushServiceSignatureFieldGenerator
import org.p2p.wallet.auth.gateway.repository.mapper.write
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import java.util.Optional

class HistoryServiceSignatureFieldGenerator(
    private val pushServiceSignatureFieldGenerator: PushServiceSignatureFieldGenerator,
    private val coroutineDispatchers: CoroutineDispatchers
) {
    suspend fun generateSignature(
        pubKey: String,
        privateKey: ByteArray,
        offset: Long,
        limit: Long,
        mint: Optional<String>
    ): Base58String = withContext(coroutineDispatchers.io) {
        return@withContext pushServiceSignatureFieldGenerator.generateSignatureField(
            userPrivateKey = privateKey.toBase58Instance(),
            structToSerialize = HistoryBorshSignature(
                pubkey = pubKey,
                offset = offset,
                limit = limit,
                mint = mint
            )
        )
    }

    inner class HistoryBorshSignature(
        private val pubkey: String,
        private val offset: Long,
        private val limit: Long,
        private val mint: Optional<String>
    ) :
        BorshSerializable {
        override fun serializeSelf(): ByteArray = getBorshBuffer()
            .write(
                pubkey,
                offset,
                limit,
                mint
            )
            .toByteArray()
    }
}
