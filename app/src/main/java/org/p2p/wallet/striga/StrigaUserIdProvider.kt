package org.p2p.wallet.striga

import org.p2p.wallet.auth.interactor.MetadataInteractor

class StrigaUserIdIsNotSet : Throwable(
    message = "Striga userId has not been set to metadata"
)

class StrigaUserIdProvider(private val metadataInteractor: MetadataInteractor) {
    fun getUserId(): String? =
        metadataInteractor.currentMetadata?.strigaMetadata?.userId

    fun getUserIdOrThrow(): String =
        getUserId() ?: throw StrigaUserIdIsNotSet()
}
