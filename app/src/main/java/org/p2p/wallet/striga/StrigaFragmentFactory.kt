package org.p2p.wallet.striga

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.math.BigDecimal
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.kyc.ui.StrigaKycFragment
import org.p2p.wallet.striga.kyc.ui.StrigaKycPendingBottomSheet
import org.p2p.wallet.striga.offramp.ui.StrigaOffRampFragment
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawFragment
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawFragmentType
import org.p2p.wallet.striga.onramp.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.signup.finish.StrigaSignupFinishFragment
import org.p2p.wallet.striga.signup.onboarding.StrigaOnboardingFragment
import org.p2p.wallet.striga.sms.onramp.StrigaOtpConfirmFragment
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

class StrigaFragmentFactory {

    fun kycFragment(): Fragment = StrigaKycFragment()

    /**
     * Usage example:
     * <pre>
     * val fragment = strigaKycFragmentFactory.onRampConfirmOtpFragment(
     *     titleAmount = "$100",
     *     challengeId = StrigaWithdrawalChallengeId("123456")
     * )
     * replaceFragmentForResult(fragment, StrigaOnRampSmsInputFragment.REQUEST_KEY, onResult = { _, _ ->
     *     Timber.e("Striga claim OTP: success")
     * })
     * </pre>
     */
    fun onRampConfirmOtpFragment(
        challengeId: StrigaWithdrawalChallengeId
    ): Fragment {
        return SmsInputFactory.create<Fragment>(
            type = SmsInputFactory.Type.StrigaOnRamp,
            args = bundleOf(
                StrigaOtpConfirmFragment.ARG_CHALLENGE_ID to challengeId
            )
        )
    }

    fun signupFlowFragment(target: StrigaUserStatusDestination): Fragment? {
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

    fun offRampFragment(): Fragment = StrigaOffRampFragment.create()

    fun withdrawUsdcFragment(amountInUsdc: BigDecimal): Fragment {
        return StrigaWithdrawFragment.create(
            StrigaWithdrawFragmentType.ConfirmUsdcWithdraw(amountInUsdc)
        )
    }

    fun withdrawEurFragment(amountInEur: BigDecimal): Fragment {
        return StrigaWithdrawFragment.create(
            StrigaWithdrawFragmentType.ConfirmEurWithdraw(amountInEur)
        )
    }

    fun showPendingBottomSheet(fragmentManager: FragmentManager) {
        StrigaKycPendingBottomSheet.show(fragmentManager)
    }
}
