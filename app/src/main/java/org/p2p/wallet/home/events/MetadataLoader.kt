package org.p2p.wallet.home.events

import org.p2p.wallet.auth.interactor.MetadataInteractor

class MetadataLoader(
    private val metadataInteractor: MetadataInteractor
) : HomeScreenLoader {

    override suspend fun onLoad() {
        metadataInteractor.tryLoadAndSaveMetadata()
    }

    override suspend fun onRefresh(): Unit = Unit
}
