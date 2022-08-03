package org.p2p.wallet.common.delegates

/**
 * [List] based implementation of [DelegateAdapter]
 *
 * @param T is a data object type
 * @param delegatesManager is a delegates manager which will be used to creating and binding views.
 * @param initialItems is a list of items which is used after initialisation.
 */
open class ListDelegatesAdapter<T : Any>(
    delegatesManager: AdapterDelegatesManager? = null,
    initialItems: List<T> = listOf()
) : DelegateAdapter<T>(delegatesManager) {

    var items: List<T> = initialItems
        private set(value) {
            field = value.toList()
        }
        get() = field.toList()

    override fun getItem(position: Int): T = items[position]

    override fun getItemCount(): Int = items.size

    fun updateItems(items: List<T>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun addItems(items: List<T>) = updateItems(this.items.plus(items))

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return if (item is Identifiable) item.id else super.getItemId(position)
    }
}
