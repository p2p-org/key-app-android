package com.p2p.wowlet.utils.viewbinding

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

@PublishedApi
internal class FragmentViewBindingPropertyDelegate<TBinding : ViewBinding>(
    viewBinder: (Fragment) -> TBinding
) : ViewBindingPropertyDelegate<Fragment, TBinding>(viewBinder) {
    override fun getLifecycleOwner(thisRef: Fragment): LifecycleOwner = thisRef.viewLifecycleOwner
}