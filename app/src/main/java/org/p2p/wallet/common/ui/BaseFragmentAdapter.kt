package org.p2p.wallet.common.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import android.util.SparseArray
import kotlin.reflect.KClass
import org.p2p.wallet.utils.instantiate

class BaseFragmentAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val pages: List<FragmentPageConfiguration>,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragments = SparseArray<Fragment>()

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        val pageConfig = pages[position]
        return instantiateFragment(pageConfig.kClass).also { fragment ->
            fragments.put(position, fragment)
            pageConfig.bundle?.let {
                fragment.arguments = it
            }
        }
    }

    private fun instantiateFragment(
        clazz: KClass<out Fragment>
    ): Fragment = fragmentManager.fragmentFactory.instantiate(clazz)
}
