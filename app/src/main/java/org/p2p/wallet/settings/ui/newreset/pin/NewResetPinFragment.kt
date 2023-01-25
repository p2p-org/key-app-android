package org.p2p.wallet.settings.ui.newreset.pin

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewResetPinBinding
import org.p2p.wallet.settings.ui.newreset.main.NewResetPinIntroFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val CLICK_VIBRATE_DURATION = 10L

private const val KEY_REQUEST_FORGOT_PASSWORD = "KEY_REQUEST_FORGOT_PASSWORD"
private const val KEY_RESULT_FORGOT_PASSWORD = "KEY_RESULT_FORGOT_PASSWORD"

class NewResetPinFragment :
    BaseMvpFragment<NewResetPinContract.View, NewResetPinContract.Presenter>(R.layout.fragment_new_reset_pin),
    NewResetPinContract.View {

    companion object {
        fun create(): NewResetPinFragment = NewResetPinFragment()
    }

    override val presenter: NewResetPinContract.Presenter by inject()

    private val binding: FragmentNewResetPinBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            textViewForgotPassword.setOnClickListener {
                NewForgotPinBottomSheet.show(
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
                    primaryCallback = { presenter.logout() },
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
        popBackStackTo(NewResetPinIntroFragment::class, inclusive = true)
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
