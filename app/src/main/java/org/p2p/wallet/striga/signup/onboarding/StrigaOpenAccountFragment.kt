package org.p2p.wallet.striga.signup.onboarding

import androidx.activity.addCallback
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.p2p.uikit.utils.HighlightingOption
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentStrigaOpenAccountBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepFragment
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaOpenAccountFragment : BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(
    R.layout.fragment_striga_open_account
) {

    companion object {
        fun create() = StrigaOpenAccountFragment()
    }

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()
    private val binding: FragmentStrigaOpenAccountBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { returnToMain() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            returnToMain()
        }

        binding.setupTermsAndPolicy()
        binding.buttonContinue.setOnClickListener {
            replaceFragment(StrigaSignUpFirstStepFragment.create())
        }
    }

    private fun FragmentStrigaOpenAccountBinding.setupTermsAndPolicy() {
        val termsHighlightedText = getString(R.string.striga_terms_and_policy_terms_word)
        val policyHighlightedText = getString(R.string.striga_terms_and_policy_privacy_policy_word)

        val termsCommonText = getString(
            R.string.striga_terms_and_policy, termsHighlightedText, policyHighlightedText
        )

        val spannableText = SpanUtils.highlightLinks(
            termsCommonText,
            listOf(
                HighlightingOption(
                    text = termsHighlightedText,
                    color = getColor(R.color.text_sky),
                    onClick = { navigateToTerms() }
                ),
                HighlightingOption(
                    text = policyHighlightedText,
                    color = getColor(R.color.text_sky),
                    onClick = { navigateToPrivacyPolicy() }
                )
            )
        )

        textViewTermsAndPolicy.apply {
            text = spannableText
            movementMethod = LinkMovementMethod()
        }
    }

    private fun navigateToTerms() {
        showUiKitSnackBar(message = "TBD: open terms")
    }

    private fun navigateToPrivacyPolicy() {
        showUiKitSnackBar(message = "TBD: open privacy policy")
    }

    private fun returnToMain() {
        popBackStackTo(MainContainerFragment::class)
    }
}
