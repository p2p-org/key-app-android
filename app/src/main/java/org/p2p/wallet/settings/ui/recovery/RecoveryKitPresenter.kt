package org.p2p.wallet.settings.ui.recovery

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract

class RecoveryKitPresenter(
    private val secureStorage: SecureStorageContract,
    private val resourcesProvider: ResourcesProvider
) : BasePresenter<RecoveryKitContract.View>(),
    RecoveryKitContract.Presenter {

    override fun attach(view: RecoveryKitContract.View) {
        super.attach(view)
        secureStorage.getObject(
            SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
            GatewayOnboardingMetadata::class
        )?.let { metadata ->
            with(view) {
                showDeviceName(metadata.deviceShareDeviceName)
                showPhoneNumber(metadata.customSharePhoneNumberE164)
                showSocialId(metadata.socialShareOwnerEmail)
            }
        } ?: setUnavailableState()
    }

    private fun setUnavailableState() {
        val notAvailableString = resourcesProvider.getString(R.string.recovery_not_available)
        view?.apply {
            showDeviceName(notAvailableString)
            showPhoneNumber(notAvailableString)
            showSocialId(notAvailableString)
        }
    }
}
