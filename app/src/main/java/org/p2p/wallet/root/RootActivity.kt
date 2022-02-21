package org.p2p.wallet.root

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.common.mvp.BaseMvpActivity
import org.p2p.wallet.debugdrawer.DebugDrawer
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toast

class RootActivity : BaseMvpActivity<RootContract.View, RootContract.Presenter>(), RootContract.View {

    companion object {
        fun createIntent(context: Context) = Intent(context, RootActivity::class.java)
    }

    override val presenter: RootContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        if (savedInstanceState == null) {
            presenter.openRootScreen()
        }

        presenter.loadPricesAndBids()
        initializeDebugDrawer()
    }

    override fun navigateToOnboarding() {
        replaceFragment(OnboardingFragment())
    }

    override fun navigateToSignIn() {
        replaceFragment(SignInPinFragment.create())
    }

    override fun showToast(message: Int) {
        toast(message)
    }

    override fun showToast(message: String) {
        toast(message)
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            super.onBackPressed()
        } else {
            popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeDebugDrawer() {
        val devView = findViewById<TextView>(R.id.developTextView)

        if (BuildConfig.DEBUG) {
            val drawer = DebugDrawer.install(this)

            devView.text = "${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_NAME}"
            devView.isVisible = true
            devView.setOnClickListener { drawer.openDrawer() }
            devView.setOnLongClickListener {
                val currentAlpha = devView.alpha
                if (currentAlpha == 0f) {
                    devView.alpha = 1f
                } else {
                    devView.alpha = 0f
                }
                return@setOnLongClickListener true
            }
        } else {
            devView.isVisible = false
        }
    }
}