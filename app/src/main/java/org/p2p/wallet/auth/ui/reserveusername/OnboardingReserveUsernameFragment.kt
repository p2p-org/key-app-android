package org.p2p.wallet.auth.ui.reserveusername

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputView
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputViewListener
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingReserveUsernameBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class OnboardingReserveUsernameFragment :
    BaseMvpFragment<OnboardingReserveUsernameContract.View, OnboardingReserveUsernameContract.Presenter>(
        R.layout.fragment_onboarding_reserve_username
    ),
    OnboardingReserveUsernameContract.View {

    companion object {
        fun create(): OnboardingReserveUsernameFragment = OnboardingReserveUsernameFragment()
    }

    override val presenter: OnboardingReserveUsernameContract.Presenter by inject()

    private val binding: FragmentOnboardingReserveUsernameBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.itemClose) {
                navigateToMainScreen()
                return@setOnMenuItemClickListener true
            }
            false
        }
        binding.buttonSubmitUsername.isEnabled = false
        binding.inputViewReserveUsername.listener = object : ReserveUsernameInputViewListener {
            override fun onInputChanged(newValue: String) {
                presenter.onUsernameInputChanged(newValue)
            }
        }
    }

    override fun showUsernameInvalid() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_INVALID)
        renderDisabledButton()
    }

    override fun showUsernameAvailable() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_AVAILABLE)
        renderEnabledButton()
    }

    override fun showUsernameNotAvailable() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_NOT_AVAILABLE)
        binding.buttonSubmitUsername.setText(R.string.reserve_username_create_username_button_not_available)
        renderDisabledButton()
    }

    override fun showUsernameIsChecking() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_CHECK)
        binding.buttonSubmitUsername.setText(R.string.reserve_username_create_username_button_loading)
        renderDisabledButton()
    }

    override fun showCreateUsernameFailed() {
        showUiKitSnackBar(
            messageResId = R.string.reserve_username_create_username_error,
            actionButtonResId = R.string.common_skip,
            actionBlock = { navigateToMainScreen() }
        )
    }

    private fun renderDisabledButton() {
        binding.buttonSubmitUsername.isEnabled = false
        binding.buttonSubmitUsername.backgroundTintList = requireContext().getColorStateListCompat(R.color.bg_rain)
        binding.buttonSubmitUsername.setTextColorRes(R.color.text_mountain)
    }

    private fun renderEnabledButton() {
        binding.buttonSubmitUsername.isEnabled = true
        binding.buttonSubmitUsername.backgroundTintList = requireContext().getColorStateListCompat(R.color.bg_snow)
        binding.buttonSubmitUsername.setTextColorRes(R.color.text_night)
    }

    override fun navigateToMainScreen() {
        popAndReplaceFragment(MainFragment.create(), inclusive = true)
    }

    override fun onDestroyView() {
        binding.inputViewReserveUsername.listener = null
        super.onDestroyView()
    }
}
