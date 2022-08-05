package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.common.WalletWeb3AuthManager
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentContinueOnboardingBinding
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class ContinueOnboardingFragment : BaseFragment(R.layout.fragment_continue_onboarding) {

    companion object {
        fun create(): ContinueOnboardingFragment = ContinueOnboardingFragment()
    }

    private val binding: FragmentContinueOnboardingBinding by viewBinding()

    private val walletAuthManager: WalletWeb3AuthManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            textViewContinueOnboardingSubtitle.text = getString(
                R.string.onboarding_continue_subtitle,
                walletAuthManager.getLastDeviceShare()?.userId.orEmpty()
            )
            buttonContinueOnboarding.setOnClickListener {
                replaceFragment(PhoneNumberEnterFragment.create())
            }
            buttonContinueStarting.setOnClickListener {
                popAndReplaceFragment(NewOnboardingFragment.create(), inclusive = true)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }
}
