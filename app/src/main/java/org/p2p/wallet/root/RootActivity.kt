package org.p2p.wallet.root

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.showSnackbarIndefinite
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.helpers.FragmentLoggingLifecycleListener
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.common.mvp.BaseMvpActivity
import org.p2p.wallet.databinding.ActivityRootBinding
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.solana.model.SolanaNetworkState
import org.p2p.wallet.splash.SplashFragment
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.ui.NewTransactionProgressBottomSheet
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import timber.log.Timber

class RootActivity :
    BaseMvpActivity<RootContract.View, RootContract.Presenter>(),
    RootContract.View,
    RootListener {

    companion object {
        const val ACTION_RESTART = "android.intent.action.RESTART"
        fun createIntent(context: Context, action: String = Intent.ACTION_PACKAGE_FIRST_LAUNCH): Intent =
            Intent(context, RootActivity::class.java)
                .apply { this.action = action }
    }

    private val deeplinksManager: AppDeeplinksManager by inject()

    override val presenter: RootContract.Presenter by inject()
    private val adminAnalytics: AdminAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val crashLogger: CrashLogger by inject()

    private val networkObserver: SolanaNetworkObserver by inject()
    private val decorSystemBarsDelegate = DecorSystemBarsDelegate(this)

    private lateinit var binding: ActivityRootBinding

    private var snackbar: Snackbar? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeeplink(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.WalletTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        replaceFragment(SplashFragment.create())

        adminAnalytics.logAppOpened(AdminAnalytics.AppOpenSource.DIRECT)

        onBackPressedDispatcher.addCallback {
            logScreenOpenEvent()
        }
        supportFragmentManager.registerFragmentLifecycleCallbacks(FragmentLoggingLifecycleListener(), true)

        checkForGoogleServices()
        deeplinksManager.mainFragmentManager = supportFragmentManager
        handleDeeplink()

        registerNetworkObserver()
    }

    private fun logScreenOpenEvent() {
        val openedFragment = supportFragmentManager.findFragmentById(R.id.rootContainer) as? BaseFragment
        if (openedFragment != null) {
            analyticsInteractor.logScreenOpenEvent(openedFragment.getAnalyticsName())
        } else {
            val findFragmentError = ClassCastException(
                "Can't log screen open event: fragment - ${supportFragmentManager.findFragmentById(R.id.rootContainer)}"
            )
            Timber.e(findFragmentError)
        }
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

    override fun showTransactionProgress(internalTransactionId: String, data: NewShowProgress) {
        NewTransactionProgressBottomSheet.show(supportFragmentManager, internalTransactionId, data)
    }

    private fun checkForGoogleServices() {
        val servicesAvailabilityChecker = GoogleApiAvailability.getInstance()
        val userHasGoogleServices =
            servicesAvailabilityChecker.isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
        if (userHasGoogleServices) {
            Timber.i("Google services are found on device")
        } else {
            Timber.i("Google services are NOT found on device: code=$userHasGoogleServices")
        }

        crashLogger.setCustomKey("has_google_services", userHasGoogleServices)
    }

    override fun onStop() {
        super.onStop()
        if (intent.action == ACTION_RESTART) return
        adminAnalytics.logAppClosed(analyticsInteractor.getCurrentScreenName())
    }

    override fun onDestroy() {
        deeplinksManager.mainFragmentManager = null
        super.onDestroy()
    }

    fun setDefaultSystemBarStyle() = decorSystemBarsDelegate.setDefaultSystemBarStyle()

    fun setSystemBarStyle(style: SystemIconsStyle) = decorSystemBarsDelegate.setSystemBarStyle(style)

    fun setStatusBarStyle(style: SystemIconsStyle) = decorSystemBarsDelegate.setStatusBarStyle(style)

    fun setNavigationBarStyle(style: SystemIconsStyle) = decorSystemBarsDelegate.setNavigationBarStyle(style)

    private fun handleDeeplink(newIntent: Intent? = null) {
        val intentToHandle = newIntent ?: intent
        deeplinksManager.handleDeeplinkIntent(intentToHandle)
    }

    private fun registerNetworkObserver() {
        lifecycleScope.launchWhenResumed {
            networkObserver.getStateFlow().collectLatest { state ->
                when (state) {
                    is SolanaNetworkState.Offline -> showSnackbarErrorMessage()
                    is SolanaNetworkState.Online,
                    is SolanaNetworkState.Idle -> Unit
                }
            }
        }
    }

    private fun showSnackbarErrorMessage() {
        snackbar = binding.root.showSnackbarIndefinite(
            snackbarText = getString(R.string.error_solana_network),
            snackbarActionButtonText = getString(R.string.common_hide),
            snackbarActionButtonListener = { snackbar ->
                snackbar.dismiss()
                networkObserver.setSnackbarHidden(isHidden = true)
            }
        )
    }
}
