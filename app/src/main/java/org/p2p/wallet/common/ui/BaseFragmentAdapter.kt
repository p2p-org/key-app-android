package org.p2p.wallet.common.ui

import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.p2p.wallet.utils.instantiate
import kotlin.reflect.KClass

class BaseFragmentAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val items: List<KClass<out Fragment>>,
    var args: List<Bundle?>? = null
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragments = SparseArray<Fragment>()

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment = instantiateFragment(items[position]).also { fragment ->
        fragments.put(position, fragment)
        args?.getOrNull(position)?.let {
            fragment.arguments = it
        }
    }

    private fun instantiateFragment(clazz: KClass<out Fragment>): Fragment =
        fragmentManager.fragmentFactory.instantiate(clazz)
}
