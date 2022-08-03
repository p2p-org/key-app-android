package org.p2p.wallet.common.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Base [RecyclerView.Adapter] implementation which uses [AdapterDelegatesManager] for creating
 * and binding view holders.
 *
 * @param T data object type
 * @param delegatesManager delegates manager which will be used to creating and binding views
 */
abstract class DelegateAdapter<T : Any>(
    var delegatesManager: AdapterDelegatesManager? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegatesManager?.getDelegateFor(viewType)?.onCreateViewHolder(parent)
            ?: throw AdapterDelegatesManagerNotSetException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getAdapterDelegate(holder.itemViewType)
            ?.onBindViewHolder(holder, getItem(position))
            ?: throw AdapterDelegatesManagerNotSetException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        getAdapterDelegate(holder.itemViewType)
            ?.onBindViewHolder(holder, getItem(position), payloads)
            ?: throw AdapterDelegatesManagerNotSetException()
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        getAdapterDelegate(holder.itemViewType)
            ?.onViewRecycled(holder)
            ?: throw AdapterDelegatesManagerNotSetException()
    }

    override fun getItemViewType(position: Int): Int {
        return delegatesManager?.getViewTypeFor(position, getItem(position))
            ?: throw AdapterDelegatesManagerNotSetException()
    }

    /**
     * Return item for a position.
     *
     * @param position of the item
     * @return item of specified class
     */
    abstract fun getItem(position: Int): T

    @Suppress("UNCHECKED_CAST")
    private fun getAdapterDelegate(
        itemViewType: Int
    ): RecyclerViewAdapterDelegate<Any, RecyclerView.ViewHolder>? {
        return delegatesManager?.getDelegateFor(itemViewType)
            as? RecyclerViewAdapterDelegate<Any, RecyclerView.ViewHolder>
    }
}

class AdapterDelegatesManagerNotSetException :
    IllegalStateException("AdapterDelegatesManager is not set")
