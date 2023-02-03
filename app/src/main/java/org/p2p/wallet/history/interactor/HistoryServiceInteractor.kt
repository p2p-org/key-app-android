package org.p2p.wallet.history.interactor

import org.p2p.wallet.auth.gateway.repository.mapper.BorshSerializable
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceSignatureFieldGenerator
import org.p2p.wallet.auth.gateway.repository.mapper.write
import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.wallet.history.repository.remote.RpcHistoryRemoteRepository
import org.p2p.wallet.utils.toBase58Instance

class HistoryServiceInteractor(
    private val historyServiceRepository: RpcHistoryRemoteRepository,
    private val gatewayServiceSignatureFieldGenerator: GatewayServiceSignatureFieldGenerator
) {

    suspend fun loadHistory(
        publicKey: String,
        privateKey: ByteArray,
        limit: Int,
        offset: Int
    ): List<RpcHistoryResponse> {
        val signature = gatewayServiceSignatureFieldGenerator.generateSignatureField(
            userPrivateKey = privateKey.toBase58Instance(),
            structToSerialize = HistoryBorshSignature(privateKey, limit, offset)
        )
        return historyServiceRepository.getHistory(publicKey, signature, limit, offset)
    }

    inner class HistoryBorshSignature(private val pubkey: ByteArray, private val limit: Int, private val offset: Int) :
        BorshSerializable {

        override fun serializeSelf(): ByteArray = getBorshBuffer().write(
            pubkey.contentToString(),
            offset,
            limit,
            0
        ).toByteArray()
    }
}
