package org.p2p.wallet.smsinput.striga

import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.striga.sms.StrigaSmsErrorFragment
import org.p2p.wallet.striga.sms.StrigaSmsErrorViewType
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment

class StrigaSmsInputFragment : BaseSmsInputFragment() {
    companion object {
        // we need to take into account that the first sms is sent when the user is created
        // only upon subsequent screen openings should the sms be re-sent
        const val ARG_RESEND_SMS_ON_INIT = "ARG_RESEND_SMS_ON_LAUNCH"
    }

    private val resendSmsOnLaunch: Boolean by args(ARG_RESEND_SMS_ON_INIT, true)

    override val presenter: SmsInputContract.Presenter by inject(named(SmsInputFactory.Type.Striga.name)) {
        parametersOf(resendSmsOnLaunch)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uiKitToolbar.setTitle(R.string.striga_sms_input_toolbar_title)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showExitDialog()
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.textViewTitle.text = getString(R.string.striga_sms_input_title)
        binding.textViewDescription.text =
            getString(R.string.striga_sms_input_description, userPhoneNumber.formattedValue)
    }

    override fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError, timerLeftTime: Long) = Unit
    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) = Unit
    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) = Unit

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setIcon(R.drawable.ic_key_app_circle)
            .setTitle(getString(R.string.striga_exit_sms_input_warning_dialog_title))
            .setMessage(getString(R.string.striga_exit_sms_input_warning_dialog_message))
            .setNegativeButton(R.string.striga_exit_sms_input_warning_dialog_btn_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.striga_exit_sms_input_warning_dialog_btn_positive) { _, _ ->
                popBackStackTo(MainContainerFragment::class)
            }
            .show()
    }

    override fun navigateToExceededDailyResendSmsLimit() {
        replaceFragment(StrigaSmsErrorFragment.create(viewType = StrigaSmsErrorViewType.ExceededResendAttempts()))
    }

    override fun navigateToExceededConfirmationAttempts() {
        replaceFragment(StrigaSmsErrorFragment.create(viewType = StrigaSmsErrorViewType.ExceededConfirmationAttempts()))
    }
}
