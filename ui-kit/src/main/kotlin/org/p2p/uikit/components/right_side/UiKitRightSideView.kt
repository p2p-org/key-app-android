package org.p2p.uikit.components.right_side

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.databinding.WidgetRightSideDoubleTextBinding
import org.p2p.uikit.databinding.WidgetRightSideIconWrapperBinding
import org.p2p.uikit.databinding.WidgetRightSideProgressWrapperBinding
import org.p2p.uikit.databinding.WidgetRightSideSingleTextTwoIconBinding
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.image.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitRightSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var styleType = MainCellStyle.FINANCE_BLOCK
    private var currentModel: RightSideCellModel? = null
    private val viewPool = ComponentViewPool<RightSideCellModel>(this) {
        when (this) {
            RightSideCellModel.TwoLineText::class -> inflateViewBinding<WidgetRightSideDoubleTextBinding>()
                .apply { updateStyle(styleType) }
            RightSideCellModel.SingleTextTwoIcon::class -> inflateViewBinding<WidgetRightSideSingleTextTwoIconBinding>()
                .apply { updateStyle(styleType) }
            RightSideCellModel.IconWrapper::class -> inflateViewBinding<WidgetRightSideIconWrapperBinding>()
            RightSideCellModel.Progress::class -> inflateViewBinding<WidgetRightSideProgressWrapperBinding>()
            else -> error("No type for viewPool: $this")
        }
    }

    val item: RightSideCellModel
        get() = currentModel ?: error("Not bind yet")

    init {
        if (isInEditMode) {
            inflateViewBinding<WidgetRightSideDoubleTextBinding>()
        }
        bindViewStyle(styleType)
    }

    fun setOnFirstTextClickAction(onFirstRightTextClicked: () -> Unit) {
        val bindingViews = viewPool.findPoolOfViews(RightSideCellModel.TwoLineText::class, removeIfInflate = true)
        val binding = bindingViews.first as WidgetRightSideDoubleTextBinding
        binding.textViewFirst.setOnClickListener { onFirstRightTextClicked() }
    }

    fun bindViewStyle(style: MainCellStyle) {
        styleType = style
        viewPool.getViewPool().forEach {
            when (val binding = it.value.first) {
                is WidgetRightSideDoubleTextBinding -> binding.updateStyle(style)
                is WidgetRightSideSingleTextTwoIconBinding -> binding.updateStyle(style)
            }
        }
    }

    fun bind(model: RightSideCellModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, model)
        when (model) {
            is RightSideCellModel.TwoLineText -> {
                (pair.first as WidgetRightSideDoubleTextBinding).bind(model)
            }
            is RightSideCellModel.SingleTextTwoIcon -> {
                (pair.first as WidgetRightSideSingleTextTwoIconBinding).bind(model)
            }
            is RightSideCellModel.IconWrapper ->
                (pair.first as WidgetRightSideIconWrapperBinding).bind(model)
            is RightSideCellModel.Progress -> {
                (pair.first as WidgetRightSideProgressWrapperBinding).bind(model)
            }
        }
        this.currentModel = model
    }

    private fun WidgetRightSideDoubleTextBinding.bind(model: RightSideCellModel.TwoLineText) {
        this.textViewFirst.bindOrGone(model.firstLineText)
        this.textViewSecond.bindOrGone(model.secondLineText)
    }

    private fun WidgetRightSideSingleTextTwoIconBinding.bind(model: RightSideCellModel.SingleTextTwoIcon) {
        this.textViewFirst.bindOrGone(model.text)
        this.imageViewFirstIcon.bindOrGone(model.firstIcon)
        this.imageViewSecondIcon.bindOrGone(model.secondIcon)
    }

    private fun WidgetRightSideIconWrapperBinding.bind(model: RightSideCellModel.IconWrapper) {
        this.iconWrapper.bindOrGone(model.iconWrapper)
    }

    private fun WidgetRightSideProgressWrapperBinding.bind(model: RightSideCellModel.Progress) {
        if (model.indeterminateProgressTint != null) {
            this.progressWrapper.indeterminateDrawable.setTint(getColor(model.indeterminateProgressTint))
        }
    }

    private fun WidgetRightSideDoubleTextBinding.updateStyle(style: MainCellStyle) {
        when (style) {
            MainCellStyle.FINANCE_BLOCK -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
            }
            MainCellStyle.BASE_CELL -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
            }
        }
    }

    private fun WidgetRightSideSingleTextTwoIconBinding.updateStyle(style: MainCellStyle) {
        when (style) {
            MainCellStyle.FINANCE_BLOCK,
            MainCellStyle.BASE_CELL -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewFirst.setTextColor(getColorStateList(R.color.text_mountain))
            }
        }
    }
}
