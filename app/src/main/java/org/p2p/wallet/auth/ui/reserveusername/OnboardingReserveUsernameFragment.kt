package org.p2p.wallet.auth.ui.reserveusername

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.UsernameAnalytics
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputView
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputViewListener
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameSkipEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingReserveUsernameBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_RESERVE_USERNAME_SOURCE = "ARG_RESERVE_USERNAME_SOURCE"

class OnboardingReserveUsernameFragment :
    BaseMvpFragment<OnboardingReserveUsernameContract.View, OnboardingReserveUsernameContract.Presenter>(
        R.layout.fragment_onboarding_reserve_username
    ),
    OnboardingReserveUsernameContract.View {

    companion object {
        fun create(from: ReserveUsernameOpenedFrom): OnboardingReserveUsernameFragment =
            OnboardingReserveUsernameFragment()
                .withArgs(ARG_RESERVE_USERNAME_SOURCE to from)
    }

    override val statusBarColor: Int
        get() = when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> R.color.bg_lime
            ReserveUsernameOpenedFrom.SETTINGS -> R.color.backgroundPrimary
        }
    override val navBarColor: Int
        get() = when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> R.color.bg_night
            ReserveUsernameOpenedFrom.SETTINGS -> R.color.bg_night
        }
    private val backgroundColorRes: Int
        get() = when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> R.color.bg_lime
            ReserveUsernameOpenedFrom.SETTINGS -> R.color.bg_rain
        }
    private val isNavigationBackVisible: Boolean
        get() = when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> false
            ReserveUsernameOpenedFrom.SETTINGS -> true
        }

    override val presenter: OnboardingReserveUsernameContract.Presenter by inject()

    private val binding: FragmentOnboardingReserveUsernameBinding by viewBinding()

    private val openedFromSource: ReserveUsernameOpenedFrom by args(ARG_RESERVE_USERNAME_SOURCE)
    private val isSkipEnabled: RegisterUsernameSkipEnabledFeatureToggle by inject()
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle by inject()

    private val usernameAnalytics: UsernameAnalytics by inject()

    private var clicksBeforeDebugSkip: Int = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usernameAnalytics.logUsernameCreationScreenOpened()

        if (BuildConfig.DEBUG) {
            initQaSkipInstrument()
        }

        binding.toolbar.initToolbar()

        binding.buttonSubmitUsername.isEnabled = false
        binding.inputViewReserveUsername.listener = ReserveUsernameInputViewListener(presenter::onUsernameInputChanged)
        binding.inputViewReserveUsername.usernamePostfixText = usernameDomainFeatureToggle.value

        binding.buttonSubmitUsername.setOnClickListener {
            usernameAnalytics.logCreateUsernameClicked()
            presenter.onCreateUsernameClicked()
        }

        binding.root.backgroundTintList = requireContext().getColorStateListCompat(backgroundColorRes)
    }

    private fun initQaSkipInstrument() {
        binding.imageViewBanner.setOnClickListener {
            clicksBeforeDebugSkip--
            if (clicksBeforeDebugSkip == 0) {
                close()
            } else {
                showUiKitSnackBar("Are you clicking me to skip the username..?! I dare you to click again! 🤬")
            }
        }
    }

    private fun UiKitToolbar.initToolbar() {
        if (isSkipEnabled.isFeatureEnabled) {
            inflateMenu(R.menu.menu_close)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.itemClose) {
                    usernameAnalytics.logSkipUsernameClicked()
                    close()
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
        if (isNavigationBackVisible) {
            navigationIcon = requireContext().getDrawableCompat(R.drawable.ic_back_night)
            setNavigationOnClickListener { close() }
        }
    }

    override fun showUsernameInvalid() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_INVALID)
        binding.buttonSubmitUsername.setText(R.string.reserve_username_create_username_button)
        renderDisabledButton()
    }

    override fun showUsernameAvailable() {
        binding.inputViewReserveUsername.renderState(ReserveUsernameInputView.InputState.USERNAME_AVAILABLE)
        binding.buttonSubmitUsername.setText(R.string.reserve_username_create_username_button)
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
            actionBlock = {
                usernameAnalytics.logSkipUsernameClicked()
                close()
            }
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

    override fun close() {
        when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> {
                popAndReplaceFragment(MainFragment.create(), inclusive = true)
            }
            ReserveUsernameOpenedFrom.SETTINGS -> {
                popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        binding.inputViewReserveUsername.listener = null
        super.onDestroyView()
    }
}
