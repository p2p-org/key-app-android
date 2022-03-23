package org.p2p.wallet.renbtc.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.wallet.renbtc.db.SessionDao
import org.p2p.wallet.renbtc.db.SessionEntity
import org.p2p.wallet.utils.toPublicKey

class RenDatabaseRepository(
    private val dao: SessionDao
) : RenLoaclRepository {

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
