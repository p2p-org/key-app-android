package org.p2p.wallet.utils.viewbinding

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PublishedApi
internal abstract class ViewBindingPropertyDelegate<TComponent, TBinging : ViewBinding>(
    private val viewBinder: (TComponent) -> TBinging
) : ReadOnlyProperty<TComponent, TBinging> {

    internal var viewBinding: TBinging? = null
    private val lifecycleObserver: BindingLifecycleObserver = BindingLifecycleObserver()

    override fun getValue(thisRef: TComponent, property: KProperty<*>): TBinging {
        viewBinding?.let { return it }

        getLifecycleOwner(thisRef).lifecycle.addObserver(lifecycleObserver)
        return viewBinder(thisRef).also { viewBinding = it }
    }

    protected abstract fun getLifecycleOwner(thisRef: TComponent): LifecycleOwner

    private inner class BindingLifecycleObserver : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            viewBinding = null
        }
    }
}
