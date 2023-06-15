package org.p2p.wallet.smsinput

import androidx.fragment.app.Fragment
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.replaceFragment

@Parcelize
sealed class SmsInputNavigationStrategy : Parcelable {

    object Replace : SmsInputNavigationStrategy() {
        override fun execute(sourceFragment: Fragment, destinationFragment: Fragment) {
            sourceFragment.replaceFragment(destinationFragment)
        }
    }

    @Parcelize
    data class PopAndReplace(
        val popTo: Class<out Fragment>? = null,
        val inclusive: Boolean = false
    ) : Parcelable, SmsInputNavigationStrategy() {
        override fun execute(sourceFragment: Fragment, destinationFragment: Fragment) {
            sourceFragment.popAndReplaceFragment(
                target = destinationFragment,
                popTo = popTo?.kotlin,
                inclusive = inclusive
            )
        }
    }

    abstract fun execute(sourceFragment: Fragment, destinationFragment: Fragment)
}
