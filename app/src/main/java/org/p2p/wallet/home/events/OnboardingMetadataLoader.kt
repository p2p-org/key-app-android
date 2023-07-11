package org.p2p.wallet.home.events

import org.p2p.wallet.auth.interactor.MetadataInteractor

class OnboardingMetadataLoader(
    private val metadataInteractor: MetadataInteractor
) : AppLoader {

    override suspend fun onLoad() {
        metadataInteractor.tryLoadAndSaveMetadata()
    }
}
