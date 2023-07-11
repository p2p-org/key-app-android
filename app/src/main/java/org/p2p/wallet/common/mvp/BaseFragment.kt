package org.p2p.wallet.common.mvp

import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.insets.appleInsetPadding
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.toast
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.pin.signin.SignInPinFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseFragment
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.utils.emptyString

private const val EXTRA_OVERRIDDEN_ENTER_ANIMATION = "EXTRA_OVERRIDDEN_ENTER_ANIMATION"
private const val EXTRA_OVERRIDDEN_EXIT_ANIMATION = "EXTRA_OVERRIDDEN_EXIT_ANIMATION"

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes), BaseFragmentContract {

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    protected open val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.BLACK
    protected open val customStatusBarStyle: SystemIconsStyle? = null
    protected open val customNavigationBarStyle: SystemIconsStyle? = null

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val extra = if (enter) EXTRA_OVERRIDDEN_ENTER_ANIMATION else EXTRA_OVERRIDDEN_EXIT_ANIMATION
        val animRes = arguments?.getInt(extra)?.takeIf { it != 0 }
            ?: return super.onCreateAnimation(transit, enter, nextAnim)

        requireArguments().remove(extra)

        return AnimationUtils.loadAnimation(requireContext(), animRes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(view)
    }

    override fun onResume() {
        super.onResume()
        logScreenOpenedEvent()
        updateSystemBarsStyle(customStatusBarStyle, customNavigationBarStyle)
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

    protected open fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, _ ->
            insets.systemAndIme().consume {
                view.appleInsetPadding(
                    left = this.left,
                    top = this.top,
                    right = this.right,
                    bottom = this.bottom,
                )
            }
        }
    }

    protected open fun updateSystemBarsStyle(
        statusBarStyle: SystemIconsStyle? = null,
        navigationBarStyle: SystemIconsStyle? = null,
    ) {
        val activity = (this.activity as? RootActivity)
        activity?.updateSystemBarsStyle(statusBarStyle, navigationBarStyle)
    }

    // TODO add another screens
    fun getAnalyticsName(): String = when (this) {
        is SeedPhraseFragment -> ScreenNames.OnBoarding.IMPORT_MANUAL
        is NewCreatePinFragment -> ScreenNames.OnBoarding.PIN_CREATE
        is DerivableAccountsFragment -> ScreenNames.OnBoarding.DERIVATION
        is ReserveUsernameFragment -> ScreenNames.OnBoarding.USERNAME_RESERVE
        is HomeFragment -> ScreenNames.Main.MAIN
        is SettingsFragment -> ScreenNames.Settings.MAIN
        is UsernameFragment -> ScreenNames.Settings.USERCARD
        is JupiterSwapFragment -> ScreenNames.Swap.MAIN
        is TokenHistoryFragment -> ScreenNames.Token.TOKEN_SCREEN
        is SignInPinFragment -> ScreenNames.Lock.SCREEN
        is HistoryFragment -> ScreenNames.Main.MAIN_HISTORY
        is ReceiveSolanaFragment -> ScreenNames.Receive.SOLANA
        is NewSendFragment -> ScreenNames.Send.MAIN
        else -> emptyString()
    }

    override fun showToast(message: TextContainer) {
        toast(message.getString(requireContext()))
    }

    override fun showUiKitSnackBar(
        message: String?,
        @StringRes messageResId: Int?,
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
                onDismissed = onDismissed,
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
