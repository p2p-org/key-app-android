package org.p2p.wallet.common.adapter

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import org.p2p.wallet.common.delegates.AdapterDelegatesManager
import org.p2p.wallet.common.delegates.RecyclerViewAdapterDelegate

/**
 * Pager adapter realisation based on Delegates Manager
 */
abstract class DelegatePagerAdapter<T : Parcelable>(
    private var delegatesManager: AdapterDelegatesManager
) : PagerAdapter() {

    /**
     * @param delegatesManager delegates manager who will be used to creating and binding views
     */
    fun setDelegatesManager(delegatesManager: AdapterDelegatesManager) {
        this.delegatesManager = delegatesManager
    }

    protected abstract fun getItem(position: Int): T

    @Suppress("UNCHECKED_CAST")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        delegatesManager.run {
            val item = getItem(position)
            val itemType = getViewTypeFor(position, item)
            val viewHolder = getDelegateFor(itemType).onCreateViewHolder(container)
            (getDelegateFor(itemType) as RecyclerViewAdapterDelegate<T, RecyclerView.ViewHolder>)
                .onBindViewHolder(viewHolder, item)
            container.addView(viewHolder.itemView)
            viewHolder.itemView.tag = viewHolder
            return viewHolder.itemView
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) = container.removeView(any as View)

    override fun isViewFromObject(view: View, any: Any): Boolean = view == any
}
