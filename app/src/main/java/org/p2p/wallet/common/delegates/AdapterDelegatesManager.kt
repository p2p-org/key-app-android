package org.p2p.wallet.common.delegates

/**
 * This interface describes adapter manager with base actions on adapter delegates [RecyclerViewAdapterDelegate]:
 * looking for correspond delegate by view type or class,
 * getting view type by item's position and item content,
 * adding delegate to adapter delegates manager.
 */
interface AdapterDelegatesManager {

    /**
     * Gets delegate by type.
     *
     * @param viewType view type for which we need adapter delegate
     * @return delegate for specified view type
     */
    fun getDelegateFor(viewType: Int): RecyclerViewAdapterDelegate<*, *>

    /**
     * Gets delegate of specific type.
     *
     * @param delegateClass required delegate class.
     * @return delegate for specified delegateType
     */
    fun getDelegateByClass(
        delegateClass: Class<out RecyclerViewAdapterDelegate<*, *>>
    ): RecyclerViewAdapterDelegate<*, *>

    /**
     * Returns viewType for delegate associated with concrete item by delegate's
     * [RecyclerViewAdapterDelegate.suitFor] method.
     * If more that one delegate suit for provided item first added delegate will be returned.
     *
     * @param position item position in adapter
     * @param data item
     * @return viewType for delegate associated with concrete [data] & [position]
     */
    fun getViewTypeFor(position: Int, data: Any): Int

    /**
     * Adds adapter delegate to collection.
     *
     * @param delegate new adapter delegate
     * @return ViewType associated with added delegate
     */
    fun addDelegate(delegate: RecyclerViewAdapterDelegate<*, *>): Int
}
