package org.p2p.wallet.common.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerViewAdapterDelegate is the bridge between [androidx.recyclerview.widget.RecyclerView.ViewHolder]
 * and real data. Work with [AdapterDelegatesManager].
 *
 * @param T the data object type
 * @param H the view holder type
 */
abstract class RecyclerViewAdapterDelegate<T, H : RecyclerView.ViewHolder> {

    /**
     * Creates ViewHolder.
     *
     * @param parent ViewGroup which will contain instantiated view
     * @return ViewHolder object
     */
    abstract fun onCreateViewHolder(parent: ViewGroup): H

    /**
     * Binds data to the views.
     *
     * @param holder ViewHolder with views
     * @param data data to bind
     */
    abstract fun onBindViewHolder(holder: H, data: T)

    /**
     * Binds data to the views with payloads.
     *
     * @param holder ViewHolder with views
     * @param data data to bind
     * @param payloads a non-null list of merged payloads (can be empty list if requires full update)
     */
    open fun onBindViewHolder(holder: H, data: T, payloads: List<Any>) {
        onBindViewHolder(holder, data)
    }

    /**
     * A view is recycled when a [RecyclerView.LayoutManager] decides that it no longer needs to be attached to
     * its parent RecyclerView. This can be because it has fallen out of visibility or a set of cached views
     * represented by views still attached to the parent RecyclerView. If an item view has large or expensive data
     * bound to it such as large bitmaps, this may be a good place to release those resources.
     *
     * @param holder The ViewHolder for the view being recycled
     */
    open fun onViewRecycled(holder: H) = Unit

    /**
     * Check that current delegate is suit for concrete item.
     *
     * @param position item position in adapter
     * @param data item
     * @return true if delegate suit for `position` & `data`
     */
    abstract fun suitFor(position: Int, data: Any): Boolean
}
