package org.p2p.wallet.auth.ui.reserveusername

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.UsernameAnalytics
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputView
import org.p2p.wallet.auth.ui.reserveusername.widget.ReserveUsernameInputViewListener
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameSkipEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReserveUsernameBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction.PlayAnimation
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction.ShowSnackbar
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_RESERVE_USERNAME_SOURCE = "ARG_RESERVE_USERNAME_SOURCE"

class ReserveUsernameFragment :
    BaseMvpFragment<ReserveUsernameContract.View, ReserveUsernameContract.Presenter>(
        R.layout.fragment_reserve_username
    ),
    ReserveUsernameContract.View {

    companion object {
        fun create(from: ReserveUsernameOpenedFrom): ReserveUsernameFragment =
            ReserveUsernameFragment()
                .withArgs(ARG_RESERVE_USERNAME_SOURCE to from)
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
    private val buttonTextColor: Int
        get() = when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> R.color.text_lime
            ReserveUsernameOpenedFrom.SETTINGS -> R.color.text_rain
        }

    override val presenter: ReserveUsernameContract.Presenter by inject()

    private val binding: FragmentReserveUsernameBinding by viewBinding()

    private val openedFromSource: ReserveUsernameOpenedFrom by args(ARG_RESERVE_USERNAME_SOURCE)
    private val isSkipEnabled: RegisterUsernameSkipEnabledFeatureToggle by inject()
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle by inject()

    private val usernameAnalytics: UsernameAnalytics by inject()
    private val onboardingAnalytics: OnboardingAnalytics by inject()

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
            onboardingAnalytics.logOnboardingMerged()
            presenter.onCreateUsernameClicked()
        }

        binding.root.backgroundTintList = requireContext().getColorStateListCompat(backgroundColorRes)
        binding.buttonSubmitUsername.setTextColor(buttonTextColor)
        if (openedFromSource == ReserveUsernameOpenedFrom.SETTINGS) {
            binding.buttonSubmitUsername.strokeWidth = 1
        }
    }

    private fun initQaSkipInstrument() {
        binding.imageViewBanner.setOnClickListener {
            clicksBeforeDebugSkip--
            if (clicksBeforeDebugSkip == 0) {
                close(isUsernameCreated = false)
            } else {
                showUiKitSnackBar("Are you clicking me to skip the username..?! I dare you to click again! 🤬")
            }
        }
    }

    private fun UiKitToolbar.initToolbar() {
        if (isSkipEnabled.isFeatureEnabled) {
            setOnMenuItemClickListener {
                if (it.itemId == R.id.itemClose) {
                    usernameAnalytics.logSkipUsernameClicked()
                    close(isUsernameCreated = false)
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
        if (isNavigationBackVisible) {
            navigationIcon = requireContext().getDrawableCompat(R.drawable.ic_back_night)
            setNavigationOnClickListener { close(isUsernameCreated = false) }
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
                close(isUsernameCreated = false)
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
        binding.buttonSubmitUsername.backgroundTintList = requireContext().getColorStateListCompat(R.color.bg_night)
        binding.buttonSubmitUsername.setTextColorRes(buttonTextColor)
    }

    override fun close(isUsernameCreated: Boolean) {
        when (openedFromSource) {
            ReserveUsernameOpenedFrom.ONBOARDING -> {
                val actions = arrayListOf<MainFragmentOnCreateAction>(PlayAnimation(R.raw.raw_animation_applause))
                if (isUsernameCreated) {
                    actions.add(ShowSnackbar(R.string.reserve_username_create_username_success))
                }

                popAndReplaceFragment(
                    MainContainerFragment.create(actions),
                    inclusive = true
                )
            }
            ReserveUsernameOpenedFrom.SETTINGS -> {
                popBackStack()
                if (isUsernameCreated) {
                    showUiKitSnackBar(messageResId = R.string.reserve_username_create_username_success)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.inputViewReserveUsername.listener = null
        super.onDestroyView()
    }
}
