package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.common.WalletWeb3AuthManager
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.utils.replaceFragment

class OnboardingRootFragment : BaseFragment(R.layout.fragment_root_onboarding) {

    companion object {
        fun create(): OnboardingRootFragment = OnboardingRootFragment()
    }

    private val authManager: WalletWeb3AuthManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            replaceFragment(
                if (authManager.isDeviceShareSaved()) {
                    ContinueOnboardingFragment.create()
                } else {
                    NewOnboardingFragment.create()
                },
                containerId = R.id.onboardingRootContainer,
                fragmentManager = childFragmentManager,
                addToBackStack = false
            )
        }
    }
}
