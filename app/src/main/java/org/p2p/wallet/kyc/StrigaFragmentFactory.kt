package org.p2p.wallet.kyc

import androidx.fragment.app.Fragment
import org.p2p.core.common.TextContainer
import org.p2p.wallet.kyc.model.StrigaKycSignUpStatus
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.model.BankTransferNavigationTarget
import org.p2p.wallet.striga.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepFragment

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

    fun bankTransferFragment(target: BankTransferNavigationTarget): Fragment? {
        return when (target) {
            BankTransferNavigationTarget.StrigaOnboarding -> StrigaOnboardingFragment.create()
            BankTransferNavigationTarget.StrigaSignupFirstStep -> StrigaSignUpFirstStepFragment.create()
            BankTransferNavigationTarget.StrigaSignupSecondStep -> StrigaSignUpFirstStepFragment.create()
            BankTransferNavigationTarget.StrigaSmsVerification -> {
                SmsInputFactory.create(
                    type = SmsInputFactory.Type.Striga,
                    destinationFragment = StrigaSignupFinishFragment::class.java
                )
            }
            BankTransferNavigationTarget.SumSubVerification -> {
                null
            }
            BankTransferNavigationTarget.Nowhere -> null
        }
    }
}
