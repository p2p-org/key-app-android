package org.p2p.wallet.relay

import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.utils.toBase58Instance

class RelayRemoteRepository(
    private val relaySdkFacade: RelaySdkFacade
) : RelayRepository {

    override suspend fun signTransaction(transaction: String, keypair: String, blockhash: RecentBlockhash): String =
        relaySdkFacade.signTransaction(
            transaction = transaction.toBase58Instance(),
            keyPair = keypair.toBase58Instance(),
            recentBlockhash = blockhash
        )
            .transaction
            .base58Value
}
