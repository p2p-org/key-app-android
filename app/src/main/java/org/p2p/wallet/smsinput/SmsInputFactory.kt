package org.p2p.wallet.smsinput

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import android.os.Bundle
import org.p2p.wallet.common.NavigationStrategy
import org.p2p.wallet.common.NavigationStrategy.Companion.ARG_NAVIGATION_STRATEGY
import org.p2p.wallet.common.NavigationStrategy.Companion.ARG_NEXT_DESTINATION_ARGS
import org.p2p.wallet.common.NavigationStrategy.Companion.ARG_NEXT_DESTINATION_CLASS
import org.p2p.wallet.settings.ui.security.SecurityAndPrivacyFragment
import org.p2p.wallet.smsinput.onboarding.OnboardingSmsInputFragment
import org.p2p.wallet.smsinput.striga.StrigaSmsInputFragment
import org.p2p.wallet.smsinput.updatedevice.UpdateDeviceSmsInputFragment

object SmsInputFactory {
    enum class Type(
        val clazz: Class<out Fragment>,
        val navigationStrategy: NavigationStrategy,
    ) {
        Onboarding(
            OnboardingSmsInputFragment::class.java,
            NavigationStrategy.PopAndReplace(null, true)
        ),
        Striga(StrigaSmsInputFragment::class.java, NavigationStrategy.Replace),
        UpdateDevice(
            UpdateDeviceSmsInputFragment::class.java,
            NavigationStrategy.PopAndReplace(SecurityAndPrivacyFragment::class.java, false)
        ),
    }

    /**
     * Creates a new instance of [BaseSmsInputFragment] with [destinationFragment] as next destination
     * @param type - type of sms input
     * @param destinationFragment - next destination fragment, will be navigated to after successful sms input
     * @param destinationArgs - arguments for the [destinationFragment]
     * @param navigationStrategy - uses this value if it's not null, otherwise - [Type.navigationStrategy]
     * @param args - extra arguments for the [BaseSmsInputFragment] and it's children
     */
    fun <T : Fragment> create(
        type: Type,
        destinationFragment: Class<T>,
        destinationArgs: Bundle? = null,
        navigationStrategy: NavigationStrategy? = null,
        args: Bundle? = null
    ): BaseSmsInputFragment {
        val fragment = requireNotNull(type.clazz.newInstance() as? BaseSmsInputFragment) { "Unknown type: $type" }

        return fragment.apply {
            arguments = bundleOf(
                ARG_NEXT_DESTINATION_CLASS to destinationFragment,
                ARG_NEXT_DESTINATION_ARGS to destinationArgs,
                ARG_NAVIGATION_STRATEGY to (navigationStrategy ?: type.navigationStrategy)
            ).also {
                it.putAll(args ?: Bundle.EMPTY)
            }
        }
    }
}
