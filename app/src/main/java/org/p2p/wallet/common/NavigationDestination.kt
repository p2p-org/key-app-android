package org.p2p.wallet.common

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.home.ui.container.MainContainerFragment

@Parcelize
data class NavigationDestination(
    val clazz: Class<out Fragment>? = null,
    val args: Bundle? = null,
    val strategy: NavigationStrategy = NavigationStrategy.PopBackStackTo(MainContainerFragment::class.java)
) : Parcelable {

    companion object {
        const val ARG_KEY = "ARG_NAVIGATION_DESTINATION"
    }

    fun navigateNext(sourceFragment: Fragment) {
        val destination = createNextDestination()
        strategy.execute(sourceFragment, destination)
    }

    private fun createNextDestination(): Fragment? {
        return clazz?.newInstance()?.apply {
            arguments = args
        }
    }
}
