package org.p2p.wallet.root

import androidx.activity.addCallback
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import org.p2p.core.utils.KeyboardListener
import org.p2p.uikit.natives.showSnackbarIndefinite
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.crashlogging.helpers.FragmentLoggingLifecycleListener
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.common.mvp.BaseMvpActivity
import org.p2p.wallet.databinding.ActivityRootBinding
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.lokalise.LokaliseService
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.solana.model.SolanaNetworkState
import org.p2p.wallet.splash.SplashFragment
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.ui.NewTransactionProgressBottomSheet
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class RootActivity :
    BaseMvpActivity<RootContract.View, RootContract.Presenter>(),
    RootContract.View,
    RootListener,
    AppActivityVisibility,
    KeyboardListener {

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
    private val decorSystemBarsDelegate by lazy { DecorSystemBarsDelegate(this) }
    private val visibilityDelegate by lazy { ActivityVisibilityDelegate(this) }
    override val keyboardState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private lateinit var binding: ActivityRootBinding

    private var snackbar: Snackbar? = null
    private var splashScreenBox: SplashScreenBox? = null
    private var onSplashDataLoaded: Boolean = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeeplink(intent)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LokaliseService.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        decorSystemBarsDelegate.onCreate()
        setupKeyboardListener()
        keepVisibleSplash(splashScreen)
        replaceFragment(SplashFragment.create(), addToBackStack = false)
        adminAnalytics.logAppOpened(AdminAnalytics.AppOpenSource.DIRECT)

        onBackPressedDispatcher.addCallback {
            logScreenOpenEvent()
        }

        checkForGoogleServices()

        supportFragmentManager.registerFragmentLifecycleCallbacks(FragmentLoggingLifecycleListener(), true)
        deeplinksManager.setRootListener(this)
        handleDeeplink()

        registerNetworkObserver()
    }

    fun hideSplashScreen() {
        onSplashDataLoaded = true
        splashScreenBox?.let {
            it.splashScreen.remove()
            decorSystemBarsDelegate.updateSystemBarsStyle(it.pendingStatusBarStyle, it.pendingNavigationBarStyle)
        }
        splashScreenBox = null
    }

    private fun keepVisibleSplash(splashScreen: SplashScreen) {
        splashScreen.setOnExitAnimationListener { splashProvider ->
            if (onSplashDataLoaded) splashProvider.remove() else splashScreenBox = SplashScreenBox(splashProvider)
        }
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            keyboardState.value = imeVisible
            insets
        }
    }

    private fun logScreenOpenEvent() {
        val openedFragment = supportFragmentManager.findFragmentById(R.id.rootContainer) as? BaseFragment
        if (openedFragment != null) {
            analyticsInteractor.logScreenOpenEvent(openedFragment.getAnalyticsName())
        } else {
            val findFragmentError = ClassCastException(
                "Can't log screen open event: fragment - ${supportFragmentManager.findFragmentById(R.id.rootContainer)}"
            )
            Timber.i(findFragmentError)
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

    override fun triggerOnboardingDeeplink(deeplink: Uri) {
        val fragment = supportFragmentManager.findFragmentById(R.id.rootContainer)
        val onboardingRootFragment = fragment as? OnboardingRootFragment

        if (onboardingRootFragment == null) {
            Timber.i("Cannot trigger onboarding deeplink, the user is not in the root onboarding screen")
            return
        }

        onboardingRootFragment.triggerOnboadringDeeplink(deeplink)
    }

    override fun popBackStackToMain() {
        with(supportFragmentManager) {
            if (backStackEntryCount > 1) {
                val lastScreen = fragments.lastOrNull()
                if (lastScreen is BottomSheetDialogFragment) lastScreen.dismissAllowingStateLoss()
                popBackStackImmediate(MainFragment::class.java.name, 0)
            }
        }
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
        deeplinksManager.clearRootListener()
        super.onDestroy()
    }

    fun updateSystemBarsStyle(
        statusBarStyle: SystemIconsStyle? = null,
        navigationBarStyle: SystemIconsStyle? = null,
    ) {
        val splashScreenBox = this.splashScreenBox
        if (!onSplashDataLoaded && splashScreenBox != null) {
            this.splashScreenBox = splashScreenBox.copy(
                pendingStatusBarStyle = statusBarStyle,
                pendingNavigationBarStyle = navigationBarStyle
            )
        } else {
            decorSystemBarsDelegate.updateSystemBarsStyle(statusBarStyle, navigationBarStyle)
        }
    }

    private fun handleDeeplink(newIntent: Intent? = null) {
        deeplinksManager.handleDeeplinkIntent(newIntent ?: intent)
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

    override val visibilityState: StateFlow<ActivityVisibility> = visibilityDelegate.getState()
}
