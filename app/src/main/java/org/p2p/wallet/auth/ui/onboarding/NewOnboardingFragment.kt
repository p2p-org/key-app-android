package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.UiKitSnackbarStyle
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
    private val analytics: OnboardingAnalytics by inject()

    private val fragments = List(1) { SliderFragment::class }
    private val args = List(1) {
        SliderFragmentArgs(
            R.drawable.onboarding_slide_temp,
            R.string.onboarding_slide_1_title,
            R.string.onboarding_slide_1_text,
        ).toBundle()
    }

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analytics.logSplashViewed()

        with(binding) {
            viewPagerOnboardingSlider.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, fragments, args)
            dotsIndicatorOnboardingSlider.attachTo(viewPagerOnboardingSlider)
            buttonCreateWalletOnboarding.setOnClickListener {
                onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                presenter.onSignUpButtonClicked()
            }
            buttonCreateWalletOnboarding.setOnLongClickListener {
                // TODO PWN-4615 remove after all onboarding testing completed!
                replaceFragment(DebugSettingsFragment.create())
                true
            }
            buttonRestoreWalletOnboarding.setOnClickListener {
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
}
