package org.p2p.uikit.components.right_side

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.components.finance_block.FinanceBlockStyle
import org.p2p.uikit.databinding.WidgetRightSideDoubleTextBinding
import org.p2p.uikit.databinding.WidgetRightSideSingleTextTwoIconBinding
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.image.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitRightSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var styleType = FinanceBlockStyle.FINANCE_BLOCK
    private var currentModel: RightSideCellModel? = null
    private val viewPool = ComponentViewPool<RightSideCellModel>(this) {
        when (this) {
            RightSideCellModel.TwoLineText::class -> inflateViewBinding<WidgetRightSideDoubleTextBinding>()
                .apply { updateStyle(styleType) }
            RightSideCellModel.SingleTextTwoIcon::class -> inflateViewBinding<WidgetRightSideSingleTextTwoIconBinding>()
                .apply { updateStyle(styleType) }
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

    fun setOnSwitchAction(
        onItemSwitchAction: (view: UiKitRightSideView, item: RightSideCellModel, isChecked: Boolean) -> Unit
    ) {

        // todo
        // example
        /*(viewPool.findPoolOfViews(RightSideUiModel.Switcher::class).first as SwitcherBinding)
            .switch.setOnCheckedChangeListener{
                onItemSwitchAction(this, currentModel as RightSideUiModel.Switcher)
            }*/
    }

    fun bindViewStyle(style: FinanceBlockStyle) {
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
            is RightSideCellModel.TwoLineText -> (pair.first as WidgetRightSideDoubleTextBinding).bind(model)
            is RightSideCellModel.SingleTextTwoIcon ->
                (pair.first as WidgetRightSideSingleTextTwoIconBinding).bind(model)
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

    private fun WidgetRightSideDoubleTextBinding.updateStyle(style: FinanceBlockStyle) {
        when (style) {
            FinanceBlockStyle.FINANCE_BLOCK -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
            }
            FinanceBlockStyle.BASE_CELL -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
            }
        }
    }

    private fun WidgetRightSideSingleTextTwoIconBinding.updateStyle(style: FinanceBlockStyle) {
        when (style) {
            FinanceBlockStyle.FINANCE_BLOCK,
            FinanceBlockStyle.BASE_CELL -> {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewFirst.setTextColor(getColorStateList(R.color.text_mountain))
            }
        }
    }
}
