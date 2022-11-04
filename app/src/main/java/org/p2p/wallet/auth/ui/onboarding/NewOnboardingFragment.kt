package org.p2p.wallet.auth.ui.onboarding

import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.auth.ui.restore.found.WalletFoundFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentNewOnboardingBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.openFile
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.io.File
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NewOnboardingFragment :
    BaseMvpFragment<NewOnboardingContract.View, NewOnboardingContract.Presenter>(R.layout.fragment_new_onboarding),
    NewOnboardingContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(): NewOnboardingFragment = NewOnboardingFragment()
    }

    override val presenter: NewOnboardingContract.Presenter by inject()
    private val onboardingInteractor: OnboardingInteractor by inject()

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE

    private val binding: FragmentNewOnboardingBinding by viewBinding()
    private val onboardingAnalytics: OnboardingAnalytics by inject()

    private val args = listOf(
        SliderFragmentArgs(
            R.drawable.onboarding_slide_1,
            R.string.onboarding_slide_1_title,
            R.string.onboarding_slide_1_text,
        ).toBundle(),
        SliderFragmentArgs(
            R.drawable.onboarding_slide_5,
            R.string.onboarding_slide_5_title,
            R.string.onboarding_slide_5_text,
        ).toBundle(),
        SliderFragmentArgs(
            R.drawable.onboarding_slide_2,
            R.string.onboarding_slide_2_title,
            R.string.onboarding_slide_2_text,
        ).toBundle(),
        SliderFragmentArgs(
            R.drawable.onboarding_slide_3,
            R.string.onboarding_slide_3_title,
            R.string.onboarding_slide_3_text,
        ).toBundle(),
        // TODO PWN-5663 temporary disable earn slide for this release
        /*SliderFragmentArgs(
            R.drawable.onboarding_slide_4,
            R.string.onboarding_slide_4_title,
            R.string.onboarding_slide_4_text,
        ).toBundle(),*/
    )
    private val fragments = List(args.size) { SliderFragment::class }

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    private var creationProgressJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingAnalytics.logSplashViewed()

        with(binding) {
            viewPagerOnboardingSlider.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, fragments, args)
            dotsIndicatorOnboardingSlider.attachTo(viewPagerOnboardingSlider)
            buttonCreateWalletOnboarding.setOnClickListener {
                onboardingAnalytics.logCreateWalletClicked()
                onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                presenter.onSignUpButtonClicked()
            }

            buttonRestoreWalletOnboarding.setOnClickListener {
                onboardingAnalytics.logAlreadyHaveWalletClicked()
                replaceFragment(CommonRestoreFragment.create())
            }
            textViewTermsAndPolicy.apply {
                text = SpanUtils.buildTermsAndPolicyText(
                    context = requireContext(),
                    onTermsClick = { presenter.onTermsClick() },
                    onPolicyClick = { presenter.onPolicyClick() }
                )
                movementMethod = LinkMovementMethod.getInstance()
            }
            if (BuildConfig.DEBUG) {
                buttonCreateWalletOnboarding.setOnLongClickListener {
                    replaceFragment(DebugSettingsFragment.create())
                    true
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun onSameTokenFoundError() {
        requireView().post {
            replaceFragment(WalletFoundFragment.create())
        }
    }

    override fun onSuccessfulSignUp() {
        requireView().post {
            replaceFragment(PhoneNumberEnterFragment.create())
        }
    }

    override fun setButtonLoadingState(isScreenLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isScreenLoading)
        with(binding) {
            buttonCreateWalletOnboarding.apply {
                isLoadingState = isScreenLoading
                isEnabled = !isScreenLoading
            }
            buttonRestoreWalletOnboarding.isEnabled = !isScreenLoading
        }
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            setButtonLoadingState(isScreenLoading = true)
            presenter.setIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun onConnectionError() {
        setButtonLoadingState(isScreenLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_offline_error)
    }

    override fun onCommonError() {
        setButtonLoadingState(isScreenLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    override fun showFile(file: File) {
        openFile(file)
    }

    override fun navigateToContinueCreateWallet() {
        replaceFragment(ContinueOnboardingFragment.create())
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        with(binding) {
            TransitionManager.beginDelayedTransition(root)
            loadingAnimationView.isVisible = isScreenLoading
            animationView.apply {
                if (isScreenLoading) {
                    setSystemBarsColors(statusBarColor, R.color.bg_lime)
                    startCreationProgressJob()
                    playAnimation()
                } else {
                    setSystemBarsColors(statusBarColor, navBarColor)
                    creationProgressJob?.cancel()
                    cancelAnimation()
                }
            }
        }
    }

    private fun startCreationProgressJob() {
        creationProgressJob = listOf(
            TimerState(R.string.onboarding_loading_title, withProgress = false),
            TimerState(R.string.onboarding_loading_title_1),
            TimerState(R.string.onboarding_loading_title_2),
            TimerState(R.string.onboarding_loading_title_3),
        ).asSequence()
            .asFlow()
            .onEach {
                with(binding) {
                    textViewCreationTitle.setText(it.titleRes)
                    textViewCreationMessage.isVisible = !it.withProgress
                    progressBarCreation.isVisible = it.withProgress
                }
                delay(2.seconds.inWholeMilliseconds)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        creationProgressJob?.cancel()
        super.onDestroyView()
    }

    data class TimerState(
        @StringRes val titleRes: Int,
        val withProgress: Boolean = true
    )
}
