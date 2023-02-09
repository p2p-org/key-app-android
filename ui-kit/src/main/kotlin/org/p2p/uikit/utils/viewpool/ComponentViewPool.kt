package org.p2p.uikit.utils.viewpool

import androidx.core.view.forEach
import androidx.viewbinding.ViewBinding
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

typealias BindingWithViews = Pair<ViewBinding, List<View>>

class ComponentViewPool<UiModel : Any>(
    private val viewGroup: ViewGroup,
    private val inflater: KClass<out UiModel>.() -> ViewBinding
) {

    private val viewPool = mutableMapOf<KClass<out UiModel>, BindingWithViews>()

    fun findPoolOfViews(type: KClass<out UiModel>, removeIfInflate: Boolean): BindingWithViews {
        return viewPool[type] ?: inflateAndSave(type)
            .also { if (removeIfInflate) viewGroup.removeAllViews() }
    }

    fun updatePoolOfViews(oldModel: UiModel?, newModel: UiModel): BindingWithViews {
        return when {
            oldModel == null -> inflateAndSave(newModel::class)
            oldModel != newModel -> updatePoolOfChildViews(newModel::class)
            else -> viewPool[newModel::class]!!
        }
    }

    private fun updatePoolOfChildViews(newUiModel: KClass<out UiModel>): BindingWithViews {
        val pair = viewPool[newUiModel]
        viewGroup.removeAllViews()
        return pair
            ?.also { it.second.forEach { view -> viewGroup.addView(view) } }
            ?: inflateAndSave(newUiModel)
    }

    private fun inflateAndSave(type: KClass<out UiModel>): BindingWithViews {
        val binding = inflater(type)
        val newView = mutableListOf<View>()
        viewGroup.forEach { newView.add(it) }
        val result = binding to newView
        viewPool[type] = result
        return result
    }
}
