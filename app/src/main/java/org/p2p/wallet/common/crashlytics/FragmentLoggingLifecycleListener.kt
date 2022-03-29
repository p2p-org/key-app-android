package org.p2p.wallet.common.crashlytics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import timber.log.Timber

class FragmentLoggingLifecycleListener : FragmentManager.FragmentLifecycleCallbacks() {
    private fun logLifecycleAction(fragment: Fragment, action: String) {
        Timber.i("LIFECYCLE ${fragment.javaClass.simpleName}: $action")
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        logLifecycleAction(f, "onFragmentViewCreated")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        logLifecycleAction(f, "onFragmentResumed")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        logLifecycleAction(f, "onFragmentPaused")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        logLifecycleAction(f, "onFragmentViewDestroyed")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        logLifecycleAction(f, "onFragmentDestroyed")
    }
}
