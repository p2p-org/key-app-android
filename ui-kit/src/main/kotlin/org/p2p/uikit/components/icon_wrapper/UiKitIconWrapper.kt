package org.p2p.uikit.components.icon_wrapper

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.uikit.databinding.WidgetIconWrapperSingleBinding
import org.p2p.uikit.databinding.WidgetIconWrapperTwoBinding
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.image.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitIconWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val constraintSet = ConstraintSet()
    private var currentModel: IconWrapperCellModel? = null
    private val viewPool = ComponentViewPool<IconWrapperCellModel>(this) {
        when (this) {
            IconWrapperCellModel.SingleIcon::class -> inflateViewBinding<WidgetIconWrapperSingleBinding>()
            IconWrapperCellModel.SingleEmoji::class -> inflateViewBinding<WidgetIconWrapperSingleBinding>()
            IconWrapperCellModel.TwoIcon::class -> inflateViewBinding<WidgetIconWrapperTwoBinding>()
            else -> error("No type for viewPool: $this")
        }
    }

    init {
        if (isInEditMode) {
            inflateViewBinding<WidgetIconWrapperTwoBinding>()
        }
    }

    fun bindOrGone(model: IconWrapperCellModel?) {
        isVisible = model != null
        model?.let { bind(it) }
    }

    fun bind(model: IconWrapperCellModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, model)
        when (model) {
            is IconWrapperCellModel.SingleIcon -> (pair.first as WidgetIconWrapperSingleBinding).bind(model)
            is IconWrapperCellModel.TwoIcon -> (pair.first as WidgetIconWrapperTwoBinding).bind(model)
            is IconWrapperCellModel.SingleEmoji -> (pair.first as WidgetIconWrapperSingleBinding).bind(model)
        }
        this.currentModel = model
    }

    private fun WidgetIconWrapperSingleBinding.bind(model: IconWrapperCellModel.SingleEmoji) {
        this.emojiViewIcon.isVisible = true
        this.imageViewIcon.isVisible = false
        this.emojiViewIcon.bind(TextContainer(model.emoji))
    }

    private fun WidgetIconWrapperSingleBinding.bind(model: IconWrapperCellModel.SingleIcon) {
        this.emojiViewIcon.isVisible = false
        this.imageViewIcon.isVisible = true
        this.imageViewIcon.bind(model.icon)

        if (model.sizePx != null) {
            val layoutParams = root.layoutParams as LayoutParams
            root.layoutParams = layoutParams.apply {
                width = model.sizePx
                height = model.sizePx
                verticalBias = 0.5f
            }
        }
    }

    private fun WidgetIconWrapperTwoBinding.bind(model: IconWrapperCellModel.TwoIcon) {
        this.imageViewFirstIcon.bindOrGone(model.first)
        this.imageViewSecondIcon.bindOrGone(model.second)

        constraintSet.clone(this@UiKitIconWrapper)
        when (model.angleType) {
            TwoIconAngle.Plus180 -> {
                constraintSet.setHorizontalBias(imageViewFirstIcon.id, 1.0f)
                constraintSet.setVerticalBias(imageViewFirstIcon.id, 0.5f)
                constraintSet.setHorizontalBias(imageViewSecondIcon.id, 0.0f)
                constraintSet.setVerticalBias(imageViewSecondIcon.id, 0.5f)
            }
            TwoIconAngle.Plus45 -> {
                constraintSet.setHorizontalBias(imageViewFirstIcon.id, 0.0f)
                constraintSet.setVerticalBias(imageViewFirstIcon.id, 0.0f)
                constraintSet.setHorizontalBias(imageViewSecondIcon.id, 1.0f)
                constraintSet.setVerticalBias(imageViewSecondIcon.id, 1.0f)
            }
            TwoIconAngle.Zero -> {
                constraintSet.setHorizontalBias(imageViewFirstIcon.id, 0.0f)
                constraintSet.setVerticalBias(imageViewFirstIcon.id, 0.5f)
                constraintSet.setHorizontalBias(imageViewSecondIcon.id, 1.0f)
                constraintSet.setVerticalBias(imageViewSecondIcon.id, 0.5f)
            }
        }
        constraintSet.applyTo(this@UiKitIconWrapper)
    }
}
