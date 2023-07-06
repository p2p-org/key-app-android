package org.p2p.wallet.relay

import org.p2p.solanaj.model.types.RecentBlockhashResponse
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.core.crypto.toBase58Instance

class RelayRemoteRepository(
    private val relaySdkFacade: RelaySdkFacade
) : RelayRepository {

    override suspend fun signTransaction(
        transaction: String,
        keypair: String,
        blockhash: RecentBlockhashResponse?
    ): String = relaySdkFacade.signTransaction(
        transaction = transaction.toBase58Instance(),
        keyPair = keypair.toBase58Instance(),
        recentBlockhash = blockhash
    )
        .transaction
        .base58Value
}
