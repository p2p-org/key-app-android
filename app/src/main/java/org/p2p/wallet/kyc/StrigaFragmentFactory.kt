package org.p2p.wallet.kyc

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.kyc.ui.StrigaKycFragment
import org.p2p.wallet.striga.kyc.ui.StrigaKycPendingBottomSheet
import org.p2p.wallet.striga.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.sms.onramp.StrigaClaimSmsInputFragment
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

class StrigaFragmentFactory {

    fun kycFragment(): Fragment = StrigaKycFragment()

    /**
     * Usage example:
     * <pre>
     * val fragment = strigaKycFragmentFactory.claimOtpFragment(
     *     titleAmount = "$100",
     *     challengeId = StrigaWithdrawalChallengeId("123456")
     * )
     * replaceFragmentForResult(fragment, StrigaOnRampSmsInputFragment.REQUEST_KEY, onResult = { _, _ ->
     *     Timber.e("Striga claim OTP: success")
     * })
     * </pre>
     */
    fun claimOtpFragment(
        titleAmount: String,
        challengeId: StrigaWithdrawalChallengeId
    ): Fragment {
        return SmsInputFactory.create<Fragment>(
            type = SmsInputFactory.Type.StrigaOnRamp,
            args = bundleOf(
                StrigaClaimSmsInputFragment.ARG_TITLE_AMOUNT to titleAmount,
                StrigaClaimSmsInputFragment.ARG_CHALLENGE_ID to challengeId
            )
        )
    }

    fun bankTransferFragment(target: StrigaUserStatusDestination): Fragment? {
        return when (target) {
            StrigaUserStatusDestination.ONBOARDING -> {
                StrigaOnboardingFragment.create()
            }
            StrigaUserStatusDestination.SMS_VERIFICATION -> {
                SmsInputFactory.create(
                    type = SmsInputFactory.Type.StrigaSignup,
                    destinationFragment = StrigaSignupFinishFragment::class.java
                )
            }
            StrigaUserStatusDestination.SUM_SUB_VERIFICATION -> {
                kycFragment()
            }
            StrigaUserStatusDestination.IBAN_ACCOUNT -> {
                StrigaUserIbanDetailsFragment.create()
            }
            StrigaUserStatusDestination.KYC_PENDING,
            StrigaUserStatusDestination.NONE -> {
                null
            }
        }
    }

    fun showPendingBottomSheet(fragmentManager: FragmentManager) {
        StrigaKycPendingBottomSheet.show(fragmentManager)
    }
}
