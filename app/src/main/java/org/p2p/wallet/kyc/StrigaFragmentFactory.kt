package org.p2p.wallet.kyc

import androidx.fragment.app.Fragment
import org.p2p.wallet.kyc.model.StrigaKycSignUpStatus
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepFragment
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

class StrigaFragmentFactory() {

    fun kycFragment(status: StrigaKycSignUpStatus): Fragment {
        when (status) {
            StrigaKycSignUpStatus.IDENTIFY -> TODO()
            StrigaKycSignUpStatus.PENDING -> TODO()
            StrigaKycSignUpStatus.VERIFICATION_DONE -> TODO()
            StrigaKycSignUpStatus.ACTION_REQUIRED -> TODO()
            StrigaKycSignUpStatus.REJECTED -> TODO()
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
