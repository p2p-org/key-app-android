package org.p2p.wallet.settings.ui.recovery

import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import timber.log.Timber

class RecoveryKitPresenter(
    private val secureStorage: SecureStorageContract
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
        } ?: Timber.e("Unable to get metadata!")
    }
}
