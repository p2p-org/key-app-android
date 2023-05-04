package org.p2p.wallet.settings.ui.resetpin.pin

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentResetPinBinding
import org.p2p.wallet.settings.ui.resetpin.main.ResetPinIntroFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val CLICK_VIBRATE_DURATION = 10L

private const val KEY_REQUEST_FORGOT_PASSWORD = "KEY_REQUEST_FORGOT_PASSWORD"
private const val KEY_RESULT_FORGOT_PASSWORD = "KEY_RESULT_FORGOT_PASSWORD"

class ResetPinFragment :
    BaseMvpFragment<ResetPinContract.View, ResetPinContract.Presenter>(R.layout.fragment_reset_pin),
    ResetPinContract.View {

    companion object {
        fun create(): ResetPinFragment = ResetPinFragment()
    }

    override val presenter: ResetPinContract.Presenter by inject()

    private val binding: FragmentResetPinBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            textViewForgotPassword.setOnClickListener {
                ForgotPinBottomSheet.show(
                    fm = childFragmentManager,
                    requestKey = KEY_REQUEST_FORGOT_PASSWORD,
                    resultKey = KEY_RESULT_FORGOT_PASSWORD
                )
            }

            pinView.onPinCompleted = { presenter.setPinCode(it) }
            pinView.onKeyboardClicked = { vibrate(CLICK_VIBRATE_DURATION) }
        }

        childFragmentManager.setFragmentResultListener(KEY_REQUEST_FORGOT_PASSWORD, viewLifecycleOwner) { _, bundle ->
            if (bundle.containsKey(KEY_RESULT_FORGOT_PASSWORD)) {
                // TODO: Info dialog is out of the design, update it according to the new design system
                showInfoDialog(
                    titleRes = R.string.settings_logout_title,
                    messageRes = R.string.settings_logout_message,
                    primaryButtonRes = R.string.common_logout,
                    primaryCallback = presenter::logout,
                    secondaryButtonRes = R.string.common_stay,
                    primaryButtonTextColor = R.color.systemErrorMain
                )
            }
        }
    }

    override fun navigateToOnboarding() {
        popAndReplaceFragment(OnboardingRootFragment.create(), inclusive = true)
    }

    override fun navigateBackToSettings() {
        popBackStackTo(ResetPinIntroFragment::class, inclusive = true)
    }

    override fun showPinCorrect() {
        binding.pinView.onSuccessPin()
    }

    override fun showPinConfirmation() {
        binding.contentView.setBackgroundColor(getColor(R.color.bg_lime))
        binding.textViewTitle.setText(R.string.settings_item_create_new_pin_code)
        binding.pinView.resetDotsColor()
        binding.pinView.clearPin()
        binding.textViewForgotPassword.isVisible = false
    }

    override fun showPinConfirmed() {
        binding.textViewTitle.setText(R.string.settings_item_confirm_new_pin_code)
        binding.pinView.clearPin()
    }

    override fun showMessage(messageRes: Int) {
        showUiKitSnackBar(messageResId = messageRes)
    }

    override fun showIncorrectPinError() {
        binding.pinView.startErrorAnimation()
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }
}
