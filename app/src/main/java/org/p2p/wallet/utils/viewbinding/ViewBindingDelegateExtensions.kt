package org.p2p.wallet.utils.viewbinding

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty

inline fun <reified VB : ViewBinding> Fragment.viewBinding(): ReadOnlyProperty<Any?, VB> {
    return FragmentViewBindingPropertyDelegate(this, VB::class)
}

@Suppress("UNUSED") // Since receiver restricts this function usage bounds
inline fun <reified T : ViewBinding> ComponentActivity.viewBinding(
    noinline binder: (ComponentActivity) -> T
): ReadOnlyProperty<ComponentActivity, T> = ActivityViewBindingPropertyDelegate(binder)
