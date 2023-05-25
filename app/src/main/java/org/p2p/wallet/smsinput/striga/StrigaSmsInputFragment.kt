package org.p2p.wallet.smsinput.striga

import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenFragment
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class StrigaSmsInputFragment : BaseSmsInputFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uiKitToolbar.setTitle(R.string.striga_sms_input_toolbar_title)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showExitDialog()
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.checkNumberTitleText.text =
            getString(R.string.striga_sms_input_title, userPhoneNumber.formattedValue)
    }

    override fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError, timerLeftTime: Long) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(error, timerLeftTime)
        )
    }

    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) {
        popAndReplaceFragment(OnboardingGeneralErrorFragment.create(handledState))
    }

    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(RestoreErrorScreenFragment.create(handledState))
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setIcon(R.drawable.ic_key_app_circle)
            .setTitle(getString(R.string.striga_exit_sms_input_warning_dialog_title))
            .setMessage(getString(R.string.striga_exit_sms_input_warning_dialog_message))
            .setNegativeButton(R.string.striga_exit_sms_input_warning_dialog_btn_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.striga_exit_sms_input_warning_dialog_btn_positive) { _, _ -> popBackStack() }
            .show()
    }
}
