package org.p2p.wallet.utils.viewbinding

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal class FragmentViewBindingPropertyDelegate<VB : ViewBinding> constructor(
    private val fragment: Fragment,
    private val viewBindingClass: KClass<VB>,
) : ReadOnlyProperty<Any?, VB> {

    private var binding: VB? = null

    private val bindingCleaner: LifecycleObserver by lazy {
        object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    binding = null
                    source.lifecycle.removeObserver(this)
                }
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): VB = binding ?: obtainBinding()

    private fun obtainBinding(): VB {
        val view = checkNotNull(fragment.view) {
            "ViewBinding is only valid between onCreateView and onDestroyView."
        }
        return viewBindingClass.bind(view).also(::saveBindingIfNeed)
    }

    private fun saveBindingIfNeed(binding: VB) {
        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        // Save binding if view is not destroyed
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            this.binding = binding
            // Clean binding on view destroy
            lifecycle.addObserver(bindingCleaner)
        }
    }
}
