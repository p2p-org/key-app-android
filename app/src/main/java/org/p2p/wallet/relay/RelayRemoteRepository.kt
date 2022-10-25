package org.p2p.wallet.relay

import org.p2p.wallet.sdk.facade.RelaySdkFacade

class RelayRemoteRepository(
    private val relaySdkFacade: RelaySdkFacade
) : RelayRepository {

    override suspend fun signTransaction(transaction: String, keypair: String, blockhash: String): String =
        relaySdkFacade.signTransaction(transaction, keypair, blockhash)
}
