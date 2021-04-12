package com.p2p.wallet.root

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.model.LaunchMode
import com.p2p.wallet.auth.ui.OnboardingFragment
import com.p2p.wallet.auth.ui.pincode.view.PinCodeFragment
import com.p2p.wallet.common.mvp.BaseMvpActivity
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import org.koin.android.ext.android.inject

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    override val presenter: RootContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        setTransparentBars()

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingFragment())
    }

    override fun navigateToSignIn() {
        val target = PinCodeFragment.create(
            openSplashScreen = true,
            isBackupDialog = false,
            type = LaunchMode.VERIFY
        )
        replaceFragment(target)
    }

    override fun onBackPressed() {
        popBackStack()
    }

    // todo: workaround, how to make the same bars in Android 11 with InsetsController
    private fun setTransparentBars() {
        with(window.decorView.rootView) {
            systemUiVisibility = systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}