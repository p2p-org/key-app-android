package org.p2p.wallet.settings.ui.recovery

import android.os.Build
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter

class RecoveryKitPresenter(
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
) : BasePresenter<RecoveryKitContract.View>(),
    RecoveryKitContract.Presenter {

    override fun attach(view: RecoveryKitContract.View) {
        super.attach(view)
        signUpDetailsStorage.getLastSignUpUserDetails()?.let { userDetails ->
            with(view) {
                showDeviceName(
                    Build.MANUFACTURER + " " + Build.MODEL
                )
                // TODO PWN-5216 show phone and device from request!
                showPhoneNumber("+12321312")
                showSocialId(userDetails.userId)
            }
        }
    }
}
