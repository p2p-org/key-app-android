package org.p2p.wallet.striga

import org.p2p.wallet.auth.interactor.MetadataInteractor

class StrigaUserIdProvider(
    private val metadataInteractor: MetadataInteractor
) {
    fun getUserId(): String {
        return metadataInteractor.currentMetadata?.strigaMetadata?.userId
            ?: error("Striga userId has not been set to metadata")
    }
}
