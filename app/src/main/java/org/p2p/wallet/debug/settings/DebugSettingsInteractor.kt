package org.p2p.wallet.debug.settings

import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.striga.kyc.interactor.StrigaKycInteractor
import org.p2p.wallet.striga.user.storage.StrigaStorageContract

class DebugSettingsInteractor(
    private val strigaKycInteractor: StrigaKycInteractor,
    private val metadataInteractor: MetadataInteractor,
    private val strigaStorageContract: StrigaStorageContract,
) {

    suspend fun setStrigaKycRejected() {
        strigaKycInteractor.simulateKycRejected().unwrap()
    }

    suspend fun detachStrigaUserFromMetadata() {
        val metadata = metadataInteractor.currentMetadata ?: error("Metadata is not loaded. Unable to proceed.")

        metadataInteractor.updateMetadata(metadata.copy(strigaMetadata = null))
        strigaStorageContract.clear()
    }
}
