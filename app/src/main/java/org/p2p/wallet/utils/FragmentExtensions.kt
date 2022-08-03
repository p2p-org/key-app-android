package org.p2p.wallet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlin.reflect.KClass

fun <T : Fragment> Fragment.instantiate(clazz: KClass<T>): Fragment =
    childFragmentManager.fragmentFactory.instantiate(clazz)

fun <T : Fragment> FragmentFactory.instantiate(clazz: KClass<T>): Fragment =
    instantiate(clazz.java.classLoader!!, clazz.java.name)
