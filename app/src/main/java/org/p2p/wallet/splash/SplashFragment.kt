package org.p2p.wallet.splash

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.replaceFragment

class SplashFragment :
    BaseMvpFragment<SplashContract.View, SplashContract.Presenter>(R.layout.fragment_splash),
    SplashContract.View {

    companion object {
        fun create(): SplashFragment = SplashFragment()
    }

    override val presenter: SplashContract.Presenter by inject()

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_lime

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(PhoneNumberEnterFragment.create())
    }

    override fun navigateToOnboarding() {
//        replaceFragment(OnboardingRootFragment.create())
    }

    override fun navigateToSignIn() {
//        replaceFragment(SignInPinFragment.create())
    }
}
