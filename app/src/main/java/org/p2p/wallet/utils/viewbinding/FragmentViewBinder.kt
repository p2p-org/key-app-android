package org.p2p.wallet.utils.viewbinding

import android.view.View
import androidx.fragment.app.Fragment

@PublishedApi
internal class FragmentViewBinder<T>(bindingClass: Class<T>) {

    private val bindMethod by lazy {
        bindingClass.getMethod("bind", View::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun bind(fragment: Fragment) = bindMethod.invoke(null, fragment.requireView()) as T
}
