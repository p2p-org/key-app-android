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

    private var currentModel: IconWrapperUiModel? = null
    private val viewPool = ComponentViewPool<IconWrapperUiModel>(this) {
        when (this) {
            IconWrapperUiModel.SingleIcon::class -> inflateViewBinding<WidgetIconWrapperSingleBinding>()
            IconWrapperUiModel.TwoIcon::class -> inflateViewBinding<WidgetIconWrapperTwoBinding>()
            else -> throw IllegalArgumentException("add remaining type")
        }
    }

    init {
        if (isInEditMode) {
            inflateViewBinding<WidgetIconWrapperTwoBinding>()
        }
    }

    fun bind(uiModel: IconWrapperUiModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, uiModel)
        when (uiModel) {
            is IconWrapperUiModel.SingleIcon -> (pair.first as WidgetIconWrapperSingleBinding).bind(uiModel)
            is IconWrapperUiModel.TwoIcon -> (pair.first as WidgetIconWrapperTwoBinding).bind(uiModel)
        }
        this.currentModel = uiModel
    }

    private fun WidgetIconWrapperSingleBinding.bind(uiModel: IconWrapperUiModel.SingleIcon) {
        this.imageViewIcon.bind(uiModel.icon)
    }

    private fun WidgetIconWrapperTwoBinding.bind(uiModel: IconWrapperUiModel.TwoIcon) {
        this.imageViewFirstIcon.bind(uiModel.first)
        this.imageViewSecondIcon.bind(uiModel.second)
    }
}
