package org.p2p.wallet.common

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment

@Parcelize
sealed class NavigationStrategy : Parcelable {

    companion object {
        const val ARG_NEXT_DESTINATION_CLASS = "ARG_NEXT_DESTINATION_CLASS"
        const val ARG_NEXT_DESTINATION_ARGS = "ARG_NEXT_DESTINATION_ARGS"
        const val ARG_NAVIGATION_STRATEGY = "ARG_NAVIGATION_STRATEGY"

        fun createNextDestination(
            nextDestinationClass: Class<Fragment>?,
            nextDestinationArgs: Bundle? = null
        ): Fragment? {
            return nextDestinationClass?.newInstance()?.apply {
                arguments = nextDestinationArgs
            }
        }
    }

    object Replace : NavigationStrategy() {
        override fun execute(sourceFragment: Fragment, destinationFragment: Fragment?) {
            require(destinationFragment != null) { "Destination fragment must not be null" }
            sourceFragment.replaceFragment(destinationFragment)
        }
    }

    @Parcelize
    data class PopAndReplace(
        val popTo: Class<out Fragment>? = null,
        val inclusive: Boolean = false
    ) : Parcelable, NavigationStrategy() {
        override fun execute(sourceFragment: Fragment, destinationFragment: Fragment?) {
            require(destinationFragment != null) { "Destination fragment must not be null" }
            sourceFragment.popAndReplaceFragment(
                target = destinationFragment,
                popTo = popTo?.kotlin,
                inclusive = inclusive
            )
        }
    }

    @Parcelize
    data class PopBackStackTo(
        val popTo: Class<out Fragment>,
        val inclusive: Boolean = false
    ) : Parcelable, NavigationStrategy() {
        override fun execute(sourceFragment: Fragment, destinationFragment: Fragment?) {
            sourceFragment.popBackStackTo(
                target = popTo.kotlin,
                inclusive = inclusive
            )
        }
    }

    abstract fun execute(sourceFragment: Fragment, destinationFragment: Fragment?)

    fun navigateNext(
        sourceFragment: Fragment,
        nextDestinationClass: Class<Fragment>?,
        nextDestinationArgs: Bundle? = null
    ) {
        val destination = createNextDestination(nextDestinationClass, nextDestinationArgs)
        execute(sourceFragment, destination)
    }
}
