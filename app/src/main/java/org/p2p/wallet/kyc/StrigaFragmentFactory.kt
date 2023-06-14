package org.p2p.wallet.kyc

import androidx.fragment.app.Fragment
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepFragment
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

class StrigaFragmentFactory() {

    fun kycFragment(status: StrigaKycStatusBanner): Fragment {
        when (status) {
            StrigaKycStatusBanner.IDENTIFY -> TODO()
            StrigaKycStatusBanner.PENDING -> TODO()
            StrigaKycStatusBanner.VERIFICATION_DONE -> TODO()
            StrigaKycStatusBanner.ACTION_REQUIRED -> TODO()
            StrigaKycStatusBanner.REJECTED -> TODO()
        }
    }

    fun bankTransferFragment(target: StrigaUserStatusDestination): Fragment? {
        return when (target) {
            StrigaUserStatusDestination.ONBOARDING -> {
                StrigaOnboardingFragment.create()
            }
            StrigaUserStatusDestination.SIGN_UP_FIRST_STEP,
            StrigaUserStatusDestination.SIGN_UP_SECOND_STEP -> {
                StrigaSignUpFirstStepFragment.create()
            }
            StrigaUserStatusDestination.SMS_VERIFICATION -> {
                SmsInputFactory.create(
                    type = SmsInputFactory.Type.Striga,
                    destinationFragment = StrigaSignupFinishFragment::class.java
                )
            }
            else -> {
                null
            }
        }
    }
}
