package org.p2p.wallet.striga.user.interactor

import timber.log.Timber
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor

class StrigaSignupDataEnsurerInteractor(
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val metadataInteractor: MetadataInteractor,
) {

    suspend fun ensureNeededDataLoaded() {
        if (metadataInteractor.currentMetadata == null) {
            Timber.i("Metadata is not fetched. Trying again...")
            metadataInteractor.tryLoadAndSaveMetadata().throwIfFailure()
        }

        if (strigaUserInteractor.isUserCreated()) {
            if (strigaUserInteractor.isUserVerificationStatusLoaded() && !strigaUserInteractor.isKycApproved) {
                Timber.i("Striga user status is not fetched. Trying again...")
                strigaUserInteractor.loadAndSaveUserStatusData().unwrap()
            }
            if (!strigaUserInteractor.isUserDetailsLoaded()) {
                Timber.i("Striga user signup data is not fetched. Trying again...")
                strigaSignupInteractor.loadAndSaveSignupData().unwrap()
            }
        }
    }
}
