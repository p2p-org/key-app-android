package org.p2p.uikit.atoms.icon_wrapper

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.viewbinding.ViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.p2p.uikit.databinding.UiKitIconWrapperSingleBinding
import org.p2p.uikit.databinding.UiKitIconWrapperTwoBinding
import org.p2p.uikit.utils.inflateViewBinding
import kotlin.reflect.KClass

typealias BindingWithViews = Pair<ViewBinding, List<View>>

class UiKitIconWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var currentModel: IconWrapperUiModel? = null
    private val viewPool = mutableMapOf<KClass<out IconWrapperUiModel>, BindingWithViews>()

    fun bind(uiModel: IconWrapperUiModel) {
        val currentModel = this.currentModel
        val pair = when {
            currentModel == null -> inflateNewViewType(uiModel)
            currentModel::class != uiModel::class -> tryUseViewPool(uiModel)
            else -> viewPool[currentModel::class] ?: return
        }
        when (uiModel) {
            is IconWrapperUiModel.SingleIcon -> (pair.first as UiKitIconWrapperSingleBinding).bind(uiModel)
            is IconWrapperUiModel.TwoIcon -> (pair.first as UiKitIconWrapperTwoBinding).bind(uiModel)
        }
    }

    private fun UiKitIconWrapperSingleBinding.bind(uiModel: IconWrapperUiModel.SingleIcon) {
        this.imageViewIcon.bind(uiModel.icon)
    }

    private fun UiKitIconWrapperTwoBinding.bind(uiModel: IconWrapperUiModel.TwoIcon) {
        this.imageViewFirstIcon.bind(uiModel.first)
        this.imageViewSecondIcon.bind(uiModel.second)
    }

    private fun tryUseViewPool(newViewType: IconWrapperUiModel): BindingWithViews {
        val pair = viewPool[newViewType::class]
        removeAllViews()
        return if (pair != null) {
            pair.second.forEach { addView(it) }
            pair
        } else {
            inflateNewViewType(newViewType)
        }
    }

    private fun inflateNewViewType(newViewType: IconWrapperUiModel): BindingWithViews {
        val binding = when (newViewType) {
            is IconWrapperUiModel.SingleIcon -> inflateViewBinding<UiKitIconWrapperSingleBinding>()
            is IconWrapperUiModel.TwoIcon -> inflateViewBinding<UiKitIconWrapperTwoBinding>()
        }
        val newView = mutableListOf<View>()
        this.forEach { newView.add(it) }
        val result = binding to newView
        viewPool[newViewType::class] = result
        return result
    }
}