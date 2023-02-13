package org.p2p.uikit.utils.viewpool

import androidx.core.view.forEach
import androidx.viewbinding.ViewBinding
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

typealias BindingWithViews = Pair<ViewBinding, List<View>>

class ComponentViewPool<CellModel : Any>(
    private val viewGroup: ViewGroup,
    private val inflater: KClass<out CellModel>.() -> ViewBinding
) {

    private val viewPool = mutableMapOf<KClass<out CellModel>, BindingWithViews>()

    fun findPoolOfViews(type: KClass<out CellModel>, removeIfInflate: Boolean): BindingWithViews {
        return viewPool[type] ?: inflateAndSave(type)
            .also { if (removeIfInflate) viewGroup.removeAllViews() }
    }

    fun updatePoolOfViews(oldModel: CellModel?, newModel: CellModel): BindingWithViews {
        return when {
            oldModel == null -> inflateAndSave(newModel::class)
            oldModel != newModel -> updatePoolOfChildViews(newModel::class)
            else -> viewPool[newModel::class]!!
        }
    }

    private fun updatePoolOfChildViews(newModel: KClass<out CellModel>): BindingWithViews {
        val pair = viewPool[newModel]
        viewGroup.removeAllViews()
        return pair
            ?.also { it.second.forEach { view -> viewGroup.addView(view) } }
            ?: inflateAndSave(newModel)
    }

    private fun inflateAndSave(type: KClass<out CellModel>): BindingWithViews {
        val binding = inflater(type)
        val newView = mutableListOf<View>()
        viewGroup.forEach { newView.add(it) }
        val result = binding to newView
        viewPool[type] = result
        return result
    }
}
