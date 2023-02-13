package org.p2p.uikit.components.icon_wrapper

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetIconWrapperSingleBinding
import org.p2p.uikit.databinding.WidgetIconWrapperTwoBinding
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitIconWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var currentModel: IconWrapperCellModel? = null
    private val viewPool = ComponentViewPool<IconWrapperCellModel>(this) {
        when (this) {
            IconWrapperCellModel.SingleIcon::class -> inflateViewBinding<WidgetIconWrapperSingleBinding>()
            IconWrapperCellModel.TwoIcon::class -> inflateViewBinding<WidgetIconWrapperTwoBinding>()
            else -> throw IllegalArgumentException("add remaining type")
        }
    }

    init {
        if (isInEditMode) {
            inflateViewBinding<WidgetIconWrapperTwoBinding>()
        }
    }

    fun bind(model: IconWrapperCellModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, model)
        when (model) {
            is IconWrapperCellModel.SingleIcon -> (pair.first as WidgetIconWrapperSingleBinding).bind(model)
            is IconWrapperCellModel.TwoIcon -> (pair.first as WidgetIconWrapperTwoBinding).bind(model)
        }
        this.currentModel = model
    }

    private fun WidgetIconWrapperSingleBinding.bind(model: IconWrapperCellModel.SingleIcon) {
        this.imageViewIcon.bind(model.icon)
    }

    private fun WidgetIconWrapperTwoBinding.bind(model: IconWrapperCellModel.TwoIcon) {
        this.imageViewFirstIcon.bind(model.first)
        this.imageViewSecondIcon.bind(model.second)
    }
}
