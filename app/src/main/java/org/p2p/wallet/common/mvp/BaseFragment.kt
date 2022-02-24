package org.p2p.wallet.common.mvp

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.p2p.wallet.auth.ui.createwallet.CreateWalletFragment
import org.p2p.wallet.auth.ui.done.AuthDoneFragment
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.security.SecurityKeyFragment
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyFragment
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.send.ui.dialogs.NetworkSelectionFragment
import org.p2p.wallet.settings.ui.network.SettingsNetworkFragment
import org.p2p.wallet.settings.ui.reset.seedinfo.SeedInfoFragment
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment

private const val EXTRA_OVERRIDDEN_ENTER_ANIMATION = "EXTRA_OVERRIDDEN_ENTER_ANIMATION"
private const val EXTRA_OVERRIDDEN_EXIT_ANIMATION = "EXTRA_OVERRIDDEN_EXIT_ANIMATION"

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes), BaseFragmentContract {

    private val analyticsInteractor: AnalyticsInteractor by inject()

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
    }

    override fun overrideEnterAnimation(@AnimRes animation: Int) {
        overrideAnimation(animation, EXTRA_OVERRIDDEN_ENTER_ANIMATION)
    }

    override fun overrideExitAnimation(@AnimRes animation: Int) {
        overrideAnimation(animation, EXTRA_OVERRIDDEN_EXIT_ANIMATION)
    }

    protected fun showSnackbar(message: String, @DrawableRes iconRes: Int?) {
        SnackBarView.make(requireView(), message, iconRes)?.show()
    }

    private fun overrideAnimation(@AnimRes animation: Int, extraKey: String) {
        arguments = (arguments ?: Bundle()).apply { putInt(extraKey, animation) }
    }
    // TODO add another screens
    fun getAnalyticsName() = when (this) {
        is CreateWalletFragment -> ScreenName.OnBoarding.WALLET_CREATE
        is SecretKeyFragment -> ScreenName.OnBoarding.IMPORT_MANUAL
        is SecurityKeyFragment -> ScreenName.OnBoarding.CREATE_MANUAL
        is CreatePinFragment -> ScreenName.OnBoarding.PIN_CREATE
        is SeedInfoFragment -> ScreenName.OnBoarding.SEED_INFO
        is VerifySecurityKeyFragment -> ScreenName.OnBoarding.SEED_VERIFY
        is DerivableAccountsFragment -> ScreenName.OnBoarding.DERIVATION
        is ReserveUsernameFragment -> ScreenName.OnBoarding.USERNAME_RESERVE
        is AuthDoneFragment -> ScreenName.OnBoarding.WELCOME_NEW_USERNAME
        is HomeFragment -> ScreenName.Main.MAIN_COINS
        is SettingsFragment -> ScreenName.Settings.MAIN
        is UsernameFragment -> ScreenName.Settings.USERCARD
        is SecurityFragment -> ScreenName.Settings.SECURITY
        is SettingsNetworkFragment -> ScreenName.Settings.NETWORK
        is NetworkSelectionFragment -> ScreenName.Send.NETWORK
        is OrcaSwapFragment -> ScreenName.Swap.MAIN
        else -> ""
    }
}