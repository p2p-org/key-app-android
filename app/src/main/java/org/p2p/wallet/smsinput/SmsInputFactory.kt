package org.p2p.wallet.smsinput

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import android.os.Bundle
import org.p2p.wallet.smsinput.onboarding.OnboardingSmsInputFragment
import org.p2p.wallet.smsinput.striga.StrigaSmsInputFragment

object SmsInputFactory {
    enum class Type(
        val clazz: Class<out Fragment>,
        val navigationStrategy: SmsInputNavigationStrategy,
    ) {
        Onboarding(OnboardingSmsInputFragment::class.java, SmsInputNavigationStrategy.PopAndReplace(null, true)),
        Striga(StrigaSmsInputFragment::class.java, SmsInputNavigationStrategy.Replace)
    }

    /**
     * Creates a new instance of [BaseSmsInputFragment] with [destinationFragment] as next destination
     * @param type - type of sms input
     * @param destinationFragment - next destination fragment, will be navigated to after successful sms input
     * @param destinationArgs - arguments for the [destinationFragment]
     * @param navigationStrategy - uses this value if it's not null, otherwise - [Type.navigationStrategy]
     */
    fun <T : Fragment> create(
        type: Type,
        destinationFragment: Class<T>,
        destinationArgs: Bundle? = null,
        navigationStrategy: SmsInputNavigationStrategy? = null
    ): BaseSmsInputFragment {
        val fragment = requireNotNull(type.clazz.newInstance() as? BaseSmsInputFragment) { "Unknown type: $type" }

        return fragment.apply {
            arguments = bundleOf(
                BaseSmsInputFragment.ARG_NEXT_DESTINATION_CLASS to destinationFragment,
                BaseSmsInputFragment.ARG_NEXT_DESTINATION_ARGS to destinationArgs,
                BaseSmsInputFragment.ARG_NAVIGATION_STRATEGY to (navigationStrategy ?: type.navigationStrategy)
            )
        }
    }
}
