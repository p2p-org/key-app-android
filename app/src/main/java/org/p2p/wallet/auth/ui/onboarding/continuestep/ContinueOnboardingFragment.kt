package org.p2p.wallet.auth.ui.onboarding.continuestep

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentContinueOnboardingBinding
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class ContinueOnboardingFragment :
    BaseMvpFragment<ContinueOnboardingContract.View, ContinueOnboardingContract.Presenter>(
        R.layout.fragment_continue_onboarding
    ),
    ContinueOnboardingContract.View {

    companion object {
        fun create(): ContinueOnboardingFragment = ContinueOnboardingFragment()
    }

    private val binding: FragmentContinueOnboardingBinding by viewBinding()

    override val presenter: ContinueOnboardingContract.Presenter by inject { parametersOf(this) }

    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE
    override val customStatusBarStyle = SystemIconsStyle.BLACK
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            buttonContinueOnboarding.setOnClickListener {
                presenter.continueSignUp()
            }
            buttonContinueStarting.setOnClickListener {
                popAndReplaceFragment(NewOnboardingFragment.create(), inclusive = true)
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
            binding.containerBottomContinueOnboarding.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun navigateToPhoneNumberEnter() {
        replaceFragment(PhoneNumberEnterFragment.create())
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
        with(binding) {
            buttonContinueOnboarding.apply {
                isLoadingState = isScreenLoading
                isEnabled = !isScreenLoading
            }
        }
    }

    override fun showUserId(userId: String) {
        binding.textViewContinueOnboardingSubtitle.text = getString(R.string.onboarding_continue_subtitle, userId)
    }
}
