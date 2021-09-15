package com.p2p.wallet.main.repository

import com.p2p.wallet.main.api.RenBTCApi
import com.p2p.wallet.main.db.SessionDao
import com.p2p.wallet.main.db.SessionEntity
import com.p2p.wallet.main.model.RenBTCPayment
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.renBridge.LockAndMint

interface RenBTCRepository {
    suspend fun getPaymentData(network: String, gateway: String): List<RenBTCPayment>
    suspend fun saveSession(session: LockAndMint.Session)
    suspend fun findSession(destinationAddress: String): LockAndMint.Session?
    suspend fun clearSessionData()
}

class RenBTCRemoteRepository(
    private val api: RenBTCApi,
    private val dao: SessionDao
) : RenBTCRepository {

    override suspend fun getPaymentData(network: String, gateway: String): List<RenBTCPayment> {
        val response = api.getPaymentData(network, gateway)
        return response.map { RenBTCPayment(it.transactionHash, it.txIndex, it.amount) }
    }

    override suspend fun saveSession(session: LockAndMint.Session) {
        val entity = SessionEntity(
            destinationAddress = session.destinationAddress.toBase58(),
            nonce = session.nonce,
            createdAt = session.createdAt,
            expiryTime = session.expiryTime,
            gatewayAddress = session.gatewayAddress
        )
        dao.insert(entity)
    }

    override suspend fun findSession(destinationAddress: String): LockAndMint.Session? {
        return dao.findByDestinationAddress(destinationAddress)?.let {
            LockAndMint.Session(
                it.destinationAddress.toPublicKey(),
                it.nonce,
                it.createdAt,
                it.expiryTime,
                it.gatewayAddress
            )
        }
    }

    override suspend fun clearSessionData() {
        dao.clearAll()
    }
}