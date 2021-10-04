package com.p2p.wallet.root.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import com.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import com.p2p.wallet.common.mvp.BaseMvpActivity
import com.p2p.wallet.debugdrawer.DebugDrawer
import com.p2p.wallet.utils.edgetoedge.applyTranslucentFlag
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import org.koin.android.ext.android.inject

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    companion object {
        fun createIntent(context: Context) = Intent(context, RootActivity::class.java)
    }

    override val presenter: RootContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        window.applyTranslucentFlag()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }

        presenter.loadPricesAndBids()
        DebugDrawer.install(this)
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingFragment())
    }

    override fun navigateToSignIn() {
        replaceFragment(SignInPinFragment.create())
    }

    override fun onBackPressed() {
        popBackStack()
    }
}