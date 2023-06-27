package org.p2p.wallet.kyc

import androidx.fragment.app.Fragment
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.kyc.ui.StrigaKycFragment
import org.p2p.wallet.striga.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

class StrigaFragmentFactory {

    fun kycFragment(): Fragment = StrigaKycFragment()

    fun bankTransferFragment(target: StrigaUserStatusDestination): Fragment? {
        return when (target) {
            StrigaUserStatusDestination.ONBOARDING -> {
                StrigaOnboardingFragment.create()
            }
            StrigaUserStatusDestination.SMS_VERIFICATION -> {
                SmsInputFactory.create(
                    type = SmsInputFactory.Type.Striga,
                    destinationFragment = StrigaSignupFinishFragment::class.java
                )
            }
            StrigaUserStatusDestination.SUM_SUB_VERIFICATION -> {
                kycFragment()
            }
            StrigaUserStatusDestination.IBAN_ACCOUNT -> {
                StrigaUserIbanDetailsFragment.create()
            }
            StrigaUserStatusDestination.NONE -> {
                null
            }
        }
    }
}
