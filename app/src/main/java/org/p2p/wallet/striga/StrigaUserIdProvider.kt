package org.p2p.wallet.striga

import org.p2p.wallet.auth.interactor.MetadataInteractor

class StrigaUserIdProvider(
    private val metadataInteractor: MetadataInteractor
) {
    fun getUserId(): String? {
        return "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"
    }

    fun getUserIdOrThrow(): String {
        return getUserId() ?: error("Striga userId has not been set to metadata")
    }
}
