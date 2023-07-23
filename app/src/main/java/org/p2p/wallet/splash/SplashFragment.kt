package org.p2p.wallet.splash

import androidx.core.app.NotificationManagerCompat
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.striga.offramp.withdraw.StrigaWithdrawFragment
import org.p2p.wallet.utils.replaceFragment

class SplashFragment :
    BaseMvpFragment<SplashContract.View, SplashContract.Presenter>(R.layout.fragment_splash),
    SplashContract.View {

    companion object {
        fun create(): SplashFragment = SplashFragment()
    }

    override val presenter: SplashContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isNotificationPermissionGranted =
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        presenter.logNotificationPermissionGranted(isNotificationPermissionGranted)
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingRootFragment.create(), addToBackStack = false)
        hideSplashScreen()
    }

    override fun navigateToSignIn() {
        replaceFragment(StrigaWithdrawFragment(), addToBackStack = false)
        hideSplashScreen()
    }

    private fun hideSplashScreen() {
        (activity as? RootActivity)?.hideSplashScreen()
    }
}
