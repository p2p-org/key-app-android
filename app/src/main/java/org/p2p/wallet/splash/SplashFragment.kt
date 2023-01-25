package org.p2p.wallet.splash

import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.replaceFragment

class SplashFragment :
    BaseMvpFragment<SplashContract.View, SplashContract.Presenter>(R.layout.fragment_splash),
    SplashContract.View {

    companion object {
        fun create(): SplashFragment = SplashFragment()
    }

    override val presenter: SplashContract.Presenter by inject()

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingRootFragment.create(), addToBackStack = false)
    }

    override fun navigateToSignIn() {
        replaceFragment(SignInPinFragment.create(), addToBackStack = false)
    }
}
