package org.p2p.wallet.common.mvp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.createwallet.CreateWalletFragment
import org.p2p.wallet.auth.ui.done.AuthDoneFragment
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.security.SecurityKeyFragment
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.send.ui.network.NetworkSelectionFragment
import org.p2p.wallet.settings.ui.network.SettingsNetworkFragment
import org.p2p.wallet.settings.ui.reset.seedinfo.SeedInfoFragment
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.emptyString

private const val EXTRA_OVERRIDDEN_ENTER_ANIMATION = "EXTRA_OVERRIDDEN_ENTER_ANIMATION"
private const val EXTRA_OVERRIDDEN_EXIT_ANIMATION = "EXTRA_OVERRIDDEN_EXIT_ANIMATION"

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes), BaseFragmentContract {

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    protected open val statusBarColor: Int = R.color.backgroundPrimary

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val extra = if (enter) EXTRA_OVERRIDDEN_ENTER_ANIMATION else EXTRA_OVERRIDDEN_EXIT_ANIMATION
        val animRes = arguments?.getInt(extra)?.takeIf { it != 0 }
            ?: return super.onCreateAnimation(transit, enter, nextAnim)

        requireArguments().remove(extra)

        return AnimationUtils.loadAnimation(requireContext(), animRes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val analyticsName = getAnalyticsName()
        if (analyticsName.isNotEmpty()) {
            analyticsInteractor.logScreenOpenEvent(analyticsName)
        }
        setStatusBarColor(statusBarColor)
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

    private fun setStatusBarColor(@ColorRes colorResId: Int) {
        val window = requireActivity().window ?: return
        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = resources.getColor(colorResId, requireActivity().theme)
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
        is CreateWalletFragment -> ScreenNames.OnBoarding.WALLET_CREATE
        is SecretKeyFragment -> ScreenNames.OnBoarding.IMPORT_MANUAL
        is SecurityKeyFragment -> ScreenNames.OnBoarding.CREATE_MANUAL
        is CreatePinFragment -> ScreenNames.OnBoarding.PIN_CREATE
        is SeedInfoFragment -> ScreenNames.OnBoarding.SEED_INFO
        is VerifySecurityKeyFragment -> ScreenNames.OnBoarding.SEED_VERIFY
        is DerivableAccountsFragment -> ScreenNames.OnBoarding.DERIVATION
        is ReserveUsernameFragment -> ScreenNames.OnBoarding.USERNAME_RESERVE
        is AuthDoneFragment -> ScreenNames.OnBoarding.WELCOME_NEW_USERNAME
        is HomeFragment -> ScreenNames.Main.MAIN_COINS
        is SettingsFragment -> ScreenNames.Settings.MAIN
        is UsernameFragment -> ScreenNames.Settings.USERCARD
        is SecurityFragment -> ScreenNames.Settings.SECURITY
        is SettingsNetworkFragment -> ScreenNames.Settings.NETWORK
        is NetworkSelectionFragment -> ScreenNames.Send.NETWORK
        is OrcaSwapFragment -> ScreenNames.Swap.MAIN
        else -> emptyString()
    }
}
