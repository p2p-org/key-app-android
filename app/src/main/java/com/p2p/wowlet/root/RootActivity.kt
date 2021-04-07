package com.p2p.wowlet.root

import android.os.Bundle
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseMvpActivity
import com.p2p.wowlet.entities.enums.PinCodeFragmentType
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.fragment.splashscreen.view.SplashScreenFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import org.koin.android.ext.android.inject

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    override val presenter: RootContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }
    }

    override fun navigateToOnboarding() {
        replaceFragment(SplashScreenFragment())
    }

    override fun navigateToSignIn() {
        val target = PinCodeFragment.create(
            openSplashScreen = true,
            isBackupDialog = false,
            type = PinCodeFragmentType.VERIFY
        )
        replaceFragment(target)
    }

    override fun onBackPressed() {
        popBackStack()
    }
}