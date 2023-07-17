package org.p2p.wallet.striga.sms.signup

import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.striga.signup.StrigaSignupModule
import org.p2p.wallet.striga.sms.error.StrigaSmsErrorFragment
import org.p2p.wallet.striga.sms.error.StrigaSmsErrorViewType
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment

class StrigaSignupSmsInputFragment : BaseSmsInputFragment() {
    override val presenter: SmsInputContract.Presenter by inject(named(StrigaSignupModule.SMS_QUALIFIER))

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

    override fun navigateToSmsInputBlocked(timerLeftTime: Long) = Unit
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
