package org.p2p.wallet.auth.ui.onboarding

import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import java.io.File
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.CreateWalletAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.auth.ui.restore.found.WalletFoundFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentNewOnboardingBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.striga.ui.firststep.StrigaSignUpFirstStepFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.openFile
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewOnboardingFragment :
    BaseMvpFragment<NewOnboardingContract.View, NewOnboardingContract.Presenter>(R.layout.fragment_new_onboarding),
    NewOnboardingContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(): NewOnboardingFragment = NewOnboardingFragment()
    }

    override val presenter: NewOnboardingContract.Presenter by inject()
    private val onboardingInteractor: OnboardingInteractor by inject()

    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE
    override val customStatusBarStyle = SystemIconsStyle.BLACK
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    private val binding: FragmentNewOnboardingBinding by viewBinding()

    private val onboardingAnalytics: OnboardingAnalytics by inject()
    private val createWalletAnalytics: CreateWalletAnalytics by inject()
    private val restoreWalletAnalytics: RestoreWalletAnalytics by inject()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingAnalytics.logSplashViewed()

        with(binding) {
            viewPagerOnboardingSlider.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, fragments, args)
            dotsIndicatorOnboardingSlider.attachTo(viewPagerOnboardingSlider)
            buttonCreateWalletOnboarding.setOnClickListener {
                createWalletAnalytics.logCreateWalletClicked()
                onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                presenter.onSignUpButtonClicked()
            }

            buttonRestoreWalletOnboarding.setOnClickListener {
                restoreWalletAnalytics.logAlreadyHaveWalletClicked()
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

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            val systemAndIme = insets.systemAndIme()
            rootView.updatePadding(top = systemAndIme.top)
            binding.containerBottomOnboarding.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
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
                setLoading(isScreenLoading)
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
        showUiKitSnackBar(messageResId = R.string.common_offline_error)
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
        if (isScreenLoading) {
            AnimationProgressFragment.show(requireActivity().supportFragmentManager, isCreation = true)
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }
}
