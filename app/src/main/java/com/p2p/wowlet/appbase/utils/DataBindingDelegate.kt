package com.p2p.gp.appbase.utils

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DataBindingDelegate<Binding : ViewDataBinding>(
    private val fragment: Fragment,
    private val layout: Int
) : ReadOnlyProperty<Fragment, Binding> {
    private var binding: Binding? = null

    init {
        fragment.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    fragment.viewLifecycleOwnerLiveData.observe(fragment) { lifecycleOwner ->
                        lifecycleOwner.lifecycle.addObserver(
                            object : DefaultLifecycleObserver {
                                override fun onDestroy(owner: LifecycleOwner) {
                                   binding=null
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): Binding {
        val binding = binding
        if (binding != null) {
            return binding
        }
        val container = thisRef.view
            ?.takeIf { it is ViewGroup }
            ?.let { it as ViewGroup }
            ?.takeIf { it.parent is ViewGroup }
            ?.let { it.parent as ViewGroup }
            ?.run { this }
        return DataBindingUtil.inflate<Binding>(thisRef.layoutInflater, layout, container, false)
            .also { this@DataBindingDelegate.binding = it }
    }

}