package org.p2p.uikit.components.left_side

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.components.finance_block.FinanceBlockStyle
import org.p2p.uikit.databinding.WidgetLeftSideIconTripleTextBinding
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone

class UiKitLeftSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetLeftSideIconTripleTextBinding>()
    private var styleType = FinanceBlockStyle.FINANCE_BLOCK

    init {
        bindViewStyle(styleType)
    }

    fun bindViewStyle(style: FinanceBlockStyle) {
        styleType = style
        when (style) {
            FinanceBlockStyle.FINANCE_BLOCK -> with(binding) {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
                textViewThird.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewThird.setTextColor(getColorStateList(R.color.text_mountain))
            }
            FinanceBlockStyle.BASE_CELL -> with(binding) {
                textViewFirst.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text3)
                textViewFirst.setTextColor(getColorStateList(R.color.text_night))
                textViewSecond.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewSecond.setTextColor(getColorStateList(R.color.text_mountain))
                textViewThird.setTextAppearance(R.style.UiKit_TextAppearance_Regular_Label1)
                textViewThird.setTextColor(getColorStateList(R.color.text_mountain))
            }
        }
    }

    fun bind(model: LeftSideCellModel) {
        when (model) {
            is LeftSideCellModel.IconWithText -> binding.bind(model)
        }
    }

    private fun WidgetLeftSideIconTripleTextBinding.bind(model: LeftSideCellModel.IconWithText) {
        imageViewIcon.isVisible = model.icon != null
        model.icon?.let { imageViewIcon.bind(it) }

        textViewFirst.bindOrGone(model.firstLineText)
        textViewSecond.bindOrGone(model.secondLineText)
        textViewThird.bindOrGone(model.thirdLineText)
    }
}
