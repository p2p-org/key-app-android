package org.p2p.wallet.common.delegates

/**
 * Default implementation of [AdapterDelegatesManager].
 */
class DefaultDelegatesManager : AdapterDelegatesManager {

    private var nextViewType = 0
    private val delegates = LinkedHashMap<Int, RecyclerViewAdapterDelegate<*, *>>()

    override fun getDelegateFor(viewType: Int): RecyclerViewAdapterDelegate<*, *> {
        return delegates[viewType] ?: throw AdapterDelegateNotFoundException(
            "No delegate found for viewType $viewType"
        )
    }

    override fun getDelegateByClass(
        delegateClass: Class<out RecyclerViewAdapterDelegate<*, *>>
    ): RecyclerViewAdapterDelegate<*, *> {
        for (delegate in delegates.values) {
            if (delegateClass.isInstance(delegate)) {
                return delegate
            }
        }
        throw AdapterDelegateNotFoundException(
            "No delegate found for delegate type " + delegateClass.simpleName
        )
    }

    override fun getViewTypeFor(position: Int, data: Any): Int {
        for ((key, value) in delegates) {
            if (value.suitFor(position, data)) {
                return key
            }
        }
        throw AdapterDelegateNotFoundException(
            "No delegate found for position $position and object $data"
        )
    }

    override fun addDelegate(delegate: RecyclerViewAdapterDelegate<*, *>): Int {
        nextViewType++.let { viewType ->
            delegates[viewType] = delegate
            return viewType
        }
    }

    fun getDelegates(): Collection<RecyclerViewAdapterDelegate<*, *>> = delegates.values.toList()
}

class AdapterDelegateNotFoundException(description: String) : IllegalStateException(description)
