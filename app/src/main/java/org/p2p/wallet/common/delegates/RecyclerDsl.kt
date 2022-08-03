package org.p2p.wallet.common.delegates

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewConfiguration<T : Any>(
    private val recyclerView: RecyclerView,
    private var layoutManager: RecyclerView.LayoutManager? = LinearLayoutManager(recyclerView.context),
    var adapter: DelegateAdapter<T>? = null
) {

    fun listAdapter(block: ListDelegatesAdapterBuilder<T>.() -> Unit) {
        val builder = ListDelegatesAdapterBuilder<T>()
        block(builder)
        adapter = builder.build()
    }

    internal fun setup() = with(recyclerView) {
        layoutManager = this@RecyclerViewConfiguration.layoutManager
        adapter = this@RecyclerViewConfiguration.adapter
    }
}

fun <T : Any> RecyclerView.withDelegatesAdapter(block: RecyclerViewConfiguration<T>.() -> Unit) =
    RecyclerViewConfiguration<T>(this).apply {
        block(this)
        setup()
    }

class ListDelegatesAdapterBuilder<T : Any>(
    private var delegatesManager: AdapterDelegatesManager? = null,
    var initialItems: List<T> = emptyList()
) {
    fun delegates(block: AdapterDelegatesManager.() -> Unit) {
        delegatesManager = DefaultDelegatesManager().apply { block(this) }
    }

    fun delegates(vararg delegates: RecyclerViewAdapterDelegate<*, *>) =
        delegates { delegates.forEach { addDelegate(it) } }

    internal fun build(): ListDelegatesAdapter<T> =
        ListDelegatesAdapter(delegatesManager, initialItems)
}

fun buildDelegatesAdapter(block: ListDelegatesAdapterBuilder<Any>.() -> Unit): ListDelegatesAdapter<Any> {
    val builder = ListDelegatesAdapterBuilder<Any>()
    block(builder)
    return builder.build()
}
