package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.common.GoogleSignInHelper
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.restore.WalletFoundFragment
import org.p2p.wallet.auth.ui.restore.WalletFoundFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentNewOnboardingBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewOnboardingFragment :
    BaseMvpFragment<NewOnboardingContract.View, NewOnboardingContract.Presenter>(R.layout.fragment_new_onboarding),
    NewOnboardingContract.View {

    companion object {
        fun create(): NewOnboardingFragment = NewOnboardingFragment()
    }

    override val presenter: NewOnboardingContract.Presenter by inject()

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
                // TODO PWN-4362 remove after all onboarding testing completed!
                replaceFragment(DebugSettingsFragment.create())
                true
            }
            buttonRestoreWalletOnboarding.setOnClickListener {
                replaceFragment(SecretKeyFragment.create())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun showError(error: String) {
        view?.post {
            setLoadingState(isScreenLoading = false)
            showErrorSnackBar(error)
        }
    }

    override fun onSameTokenError() {
        requireView().post {
            setLoadingState(isScreenLoading = false)
            replaceFragment(WalletFoundFragment.create())
        }
    }

    override fun onSuccessfulSignUp() {
        requireView().post {
            setLoadingState(isScreenLoading = false)
            replaceFragment(PhoneNumberEnterFragment.create())
        }
    }

    private fun setLoadingState(isScreenLoading: Boolean) {
        with(binding) {
            buttonCreateWalletOnboarding.apply {
                isLoading = isScreenLoading
                isEnabled = !isScreenLoading
            }
            buttonRestoreWalletOnboarding.isEnabled = !isScreenLoading
        }
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result)?.let { credential ->
            setLoadingState(isScreenLoading = true)
            presenter.setIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }
}
