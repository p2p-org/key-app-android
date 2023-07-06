package org.p2p.wallet.striga.sms.onramp

import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.striga.di.StrigaWalletModule
import org.p2p.wallet.striga.sms.error.StrigaSmsErrorFragment
import org.p2p.wallet.striga.sms.error.StrigaSmsErrorViewType
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class StrigaOnRampSmsInputFragment : BaseSmsInputFragment() {

    companion object {
        const val ARG_TITLE_AMOUNT = "ARG_TITLE_AMOUNT"
        const val ARG_CHALLENGE_ID = "ARG_CHALLENGE_ID"
        val REQUEST_KEY: String = StrigaOnRampSmsInputFragment::class.java.name
    }

    private val titleAmount: String by args(ARG_TITLE_AMOUNT)
    private val challengeId: StrigaWithdrawalChallengeId by args(ARG_CHALLENGE_ID)

    override val presenter: SmsInputContract.Presenter by inject(named(StrigaWalletModule.SMS_QUALIFIER)) {
        parametersOf(challengeId)
    }

    override fun onBackPressed() {
        popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uiKitToolbar.title = emptyString()
        binding.textViewTitle.text = getString(R.string.striga_onramp_sms_input_title, titleAmount)
    }

    override fun navigateNext() {
        setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
        super.navigateNext()
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.textViewDescription.text =
            getString(R.string.striga_onramp_sms_input_description, userPhoneNumber.formattedValue)
    }

    override fun navigateToSmsInputBlocked(timerLeftTime: Long) = Unit
    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) = Unit
    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) = Unit

    override fun navigateToExceededDailyResendSmsLimit() {
        replaceFragment(StrigaSmsErrorFragment.create(viewType = StrigaSmsErrorViewType.ExceededResendAttempts()))
    }

    override fun navigateToExceededConfirmationAttempts() {
        replaceFragment(StrigaSmsErrorFragment.create(viewType = StrigaSmsErrorViewType.ExceededConfirmationAttempts()))
    }
}
