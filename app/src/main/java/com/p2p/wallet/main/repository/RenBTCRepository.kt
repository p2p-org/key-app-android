package com.p2p.wallet.main.repository

import com.p2p.wallet.main.api.RenBTCApi
import com.p2p.wallet.main.db.SessionDao
import com.p2p.wallet.main.db.SessionEntity
import com.p2p.wallet.main.model.RenBTCPayment
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.rpc.Environment

interface RenBTCRepository {
    suspend fun getPaymentData(environment: Environment, gateway: String): List<RenBTCPayment>
    suspend fun saveSession(session: LockAndMint.Session)
    fun findSessionFlow(destinationAddress: String): Flow<LockAndMint.Session?>
    suspend fun findSession(destinationAddress: String): LockAndMint.Session?
    suspend fun clearSessionData()
}

class RenBTCRemoteRepository(
    private val api: RenBTCApi,
    private val dao: SessionDao
) : RenBTCRepository {

    override suspend fun getPaymentData(environment: Environment, gateway: String): List<RenBTCPayment> {
        val response = when (environment) {
            Environment.SOLANA,
            Environment.MAINNET -> api.getPaymentData(gateway)
            Environment.DEVNET -> api.getPaymentData("testnet", gateway)
        }
        return response.map { RenBTCPayment(it.transactionHash, it.txIndex, it.amount) }
    }

    override suspend fun saveSession(session: LockAndMint.Session) {
        val entity = SessionEntity(
            destinationAddress = session.destinationAddress.toBase58(),
            nonce = session.nonce,
            createdAt = session.createdAt,
            expiryTime = session.expiryTime,
            gatewayAddress = session.gatewayAddress,
            fee = session.fee.toString()
        )
        dao.insert(entity)
    }

    override fun findSessionFlow(destinationAddress: String): Flow<LockAndMint.Session?> {
        return dao.getSessionFlow(destinationAddress).map { session ->
            session?.let {
                LockAndMint.Session(
                    session.destinationAddress.toPublicKey(),
                    session.nonce,
                    session.createdAt,
                    session.expiryTime,
                    session.gatewayAddress,
                    session.fee.toBigInteger()
                )
            }
        }
    }

    override suspend fun findSession(destinationAddress: String): LockAndMint.Session? {
        return dao.findByDestinationAddress(destinationAddress)?.let {
            LockAndMint.Session(
                it.destinationAddress.toPublicKey(),
                it.nonce,
                it.createdAt,
                it.expiryTime,
                it.gatewayAddress,
                it.fee.toBigInteger()
            )
        }
    }

    override suspend fun clearSessionData() {
        dao.clearAll()
    }
}