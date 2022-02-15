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
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.analytics.EventInteractor
import org.p2p.wallet.common.analytics.EventsName
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.settings.ui.security.SecurityFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import timber.log.Timber

private const val EXTRA_OVERRIDDEN_ENTER_ANIMATION = "EXTRA_OVERRIDDEN_ENTER_ANIMATION"
private const val EXTRA_OVERRIDDEN_EXIT_ANIMATION = "EXTRA_OVERRIDDEN_EXIT_ANIMATION"

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes), BaseFragmentContract {

    private val eventInteractor: EventInteractor by inject()

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val extra = if (enter) EXTRA_OVERRIDDEN_ENTER_ANIMATION else EXTRA_OVERRIDDEN_EXIT_ANIMATION
        val animRes = arguments?.getInt(extra)?.takeIf { it != 0 }
            ?: return super.onCreateAnimation(transit, enter, nextAnim)

        requireArguments().remove(extra)

        return AnimationUtils.loadAnimation(requireContext(), animRes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag("_____").d("Screen opened: ${getAnalyticsName()}")
        eventInteractor.logScreenOpenEvent(getAnalyticsName())
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

    fun getAnalyticsName(): String = when (this) {
        is SettingsFragment -> EventsName.Settings.MAIN
        is UsernameFragment -> EventsName.Settings.USERCARD
        is SecurityFragment -> EventsName.Settings.SECURITY
        else -> ""
    }
}