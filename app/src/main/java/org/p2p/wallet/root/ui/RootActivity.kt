package org.p2p.wallet.root.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.common.mvp.BaseMvpActivity
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    companion object {
        fun createIntent(context: Context) = Intent(context, RootActivity::class.java)
    }

    override val presenter: RootContract.Presenter by inject()
    private lateinit var container: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        container = findViewById(R.id.content)

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }

        presenter.loadPricesAndBids()
        if (BuildConfig.DEBUG) DebugDrawer.install(this)
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingFragment())
    }

    override fun navigateToSignIn() {
        replaceFragment(SignInPinFragment.create())
    }

    override fun showToast(message: Int) {
        SnackBarView.make(container, getString(message))?.show()
    }

    override fun onBackPressed() {
        popBackStack()
    }
}