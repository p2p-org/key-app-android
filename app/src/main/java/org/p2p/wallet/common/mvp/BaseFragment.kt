package org.p2p.wallet.common.mvp

import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.security.SecurityKeyFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyFragment
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.send.ui.network.NetworkSelectionFragment
import org.p2p.wallet.settings.ui.reset.seedinfo.SeedInfoFragment
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val EXTRA_OVERRIDDEN_ENTER_ANIMATION = "EXTRA_OVERRIDDEN_ENTER_ANIMATION"
private const val EXTRA_OVERRIDDEN_EXIT_ANIMATION = "EXTRA_OVERRIDDEN_EXIT_ANIMATION"

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes), BaseFragmentContract {

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    protected val resourcesProvider: ResourcesProvider by inject()

    protected open val statusBarColor: Int = R.color.bg_snow
    protected open val navBarColor: Int = R.color.bg_snow
    protected open val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val extra = if (enter) EXTRA_OVERRIDDEN_ENTER_ANIMATION else EXTRA_OVERRIDDEN_EXIT_ANIMATION
        val animRes = arguments?.getInt(extra)?.takeIf { it != 0 }
            ?: return super.onCreateAnimation(transit, enter, nextAnim)

        requireArguments().remove(extra)

        return AnimationUtils.loadAnimation(requireContext(), animRes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSystemBarsColors(statusBarColor, navBarColor)
    }

    override fun onResume() {
        super.onResume()
        logScreenOpenedEvent()
    }

    // fragments in the tab are shown using show/hide methods
    // so no standard lifecycle methods are called
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) logScreenOpenedEvent()
    }

    private fun logScreenOpenedEvent() {
        val analyticsName = getAnalyticsName()

        if (analyticsName.isNotEmpty()) {
            analyticsInteractor.logScreenOpenEvent(analyticsName)
        } else {
            Timber.tag("ScreensAnalyticsInteractor").i("No analytic name found for screen: ${javaClass.simpleName}")
        }
    }

    override fun overrideEnterAnimation(@AnimRes animation: Int) {
        overrideAnimation(animation, EXTRA_OVERRIDDEN_ENTER_ANIMATION)
    }

    override fun overrideExitAnimation(@AnimRes animation: Int) {
        overrideAnimation(animation, EXTRA_OVERRIDDEN_EXIT_ANIMATION)
    }

    private fun overrideAnimation(@AnimRes animation: Int, extraKey: String) {
        arguments = (arguments ?: Bundle()).apply { putInt(extraKey, animation) }
    }

    protected fun setSystemBarsColors(@ColorRes colorResId: Int, @ColorRes navBarColor: Int) {
        val window = requireActivity().window ?: return
        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = resourcesProvider.getColor(colorResId)
            navigationBarColor = resourcesProvider.getColor(navBarColor)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /*
       Change status bar and its icons color
       isLight = true - white status bar, dark icons
       isLight = false - dark status bar, white icons
       Don't forget to reset status bar color on fragment destroy to restore previous color
    */
    protected fun setLightStatusBar(isLight: Boolean) {
        val window = requireActivity().window
        val decorView = window.decorView
        WindowInsetsControllerCompat(window, decorView).isAppearanceLightStatusBars = isLight
    }

    // TODO add another screens
    fun getAnalyticsName(): String = when (this) {
        is SeedPhraseFragment -> ScreenNames.OnBoarding.IMPORT_MANUAL
        is SecurityKeyFragment -> ScreenNames.OnBoarding.CREATE_MANUAL
        is NewCreatePinFragment -> ScreenNames.OnBoarding.PIN_CREATE
        is SeedInfoFragment -> ScreenNames.OnBoarding.SEED_INFO
        is VerifySecurityKeyFragment -> ScreenNames.OnBoarding.SEED_VERIFY
        is DerivableAccountsFragment -> ScreenNames.OnBoarding.DERIVATION
        is ReserveUsernameFragment -> ScreenNames.OnBoarding.USERNAME_RESERVE
        is HomeFragment -> ScreenNames.Main.MAIN
        is NewSettingsFragment -> ScreenNames.Settings.MAIN
        is UsernameFragment -> ScreenNames.Settings.USERCARD
        is SecurityFragment -> ScreenNames.Settings.SECURITY
        is NetworkSelectionFragment -> ScreenNames.Send.NETWORK
        is OrcaSwapFragment -> ScreenNames.Swap.MAIN
        is TokenHistoryFragment -> ScreenNames.Token.TOKEN_SCREEN
        is SignInPinFragment -> ScreenNames.Lock.SCREEN
        is HistoryFragment -> ScreenNames.Main.MAIN_HISTORY
        is ReceiveSolanaFragment -> ScreenNames.Receive.SOLANA
        is ReceiveNetworkTypeFragment -> ScreenNames.Receive.NETWORK
        is SendFragment -> ScreenNames.Send.MAIN
        else -> emptyString()
    }

    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) {
        require(message != null || messageResId != null) {
            "Snackbar text must be set from `message` or `messageResId` params"
        }
        val snackbarText: String = message ?: messageResId?.let(::getString)!!
        val root = requireActivity().findViewById<View>(android.R.id.content) as ViewGroup
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock,
                style = snackbarStyle
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed,
                style = snackbarStyle
            )
        }
    }
}
