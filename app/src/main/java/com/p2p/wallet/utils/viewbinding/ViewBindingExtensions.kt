package com.p2p.wallet.utils.viewbinding

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty

@Suppress("UNUSED") // Since receiver restricts this function usage bounds
inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline binder: (Fragment) -> T = FragmentViewBinder(T::class.java)::bind
): ReadOnlyProperty<Fragment, T> = FragmentViewBindingPropertyDelegate(binder)

@Suppress("UNUSED") // Since receiver restricts this function usage bounds
inline fun <reified T : ViewBinding> ComponentActivity.viewBinding(
    noinline binder: (ComponentActivity) -> T
): ReadOnlyProperty<ComponentActivity, T> = ActivityViewBindingPropertyDelegate(binder)