package org.p2p.wallet.auth.ui.onboarding

import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.auth.ui.restore.found.WalletFoundFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentNewOnboardingBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
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

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night

    private val binding: FragmentNewOnboardingBinding by viewBinding()
    private val analytics: OnboardingAnalytics by inject()

    private val fragments = List(3) { SliderFragment::class }
    private val args = List(3) {
        SliderFragmentArgs(
            R.drawable.onboarding_1_slide,
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
                isLoading = isScreenLoading
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
        binding.root.showSnackbarShort(R.string.onboarding_offline_error)
    }

    override fun onCommonError() {
        setButtonLoadingState(isScreenLoading = false)
        binding.root.showSnackbarShort(R.string.error_general_message)
    }
}
