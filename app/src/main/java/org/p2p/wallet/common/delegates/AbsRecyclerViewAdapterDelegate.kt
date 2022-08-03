package org.p2p.wallet.common.delegates

import androidx.recyclerview.widget.RecyclerView

/**
 * Extended [RecyclerViewAdapterDelegate].
 * Its [suitFor] method is based on the rule provided via constructor.
 *
 * @author Alexander Ivanov
 * @param H the view holder type
 * @param T the data object type
 * @property rule decides either the delegate is suitable for provided data or not
 * @constructor creates instance of [AbsRecyclerViewAdapterDelegate].
 */
abstract class AbsRecyclerViewAdapterDelegate<T, H : RecyclerView.ViewHolder>(
    private val rule: (position: Int, data: Any) -> Boolean
) : RecyclerViewAdapterDelegate<T, H>() {

    override fun suitFor(position: Int, data: Any): Boolean = rule(position, data)
}
