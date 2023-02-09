package org.p2p.uikit.components.right_side

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetRightSideDoubleTextBinding
import org.p2p.uikit.databinding.WidgetRightSideSingleTextTwoIconBinding
import org.p2p.uikit.utils.image.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitRightSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var currentModel: RightSideUiModel? = null
    private val viewPool = ComponentViewPool<RightSideUiModel>(this) {
        when (this) {
            RightSideUiModel.TwoLineText::class -> inflateViewBinding<WidgetRightSideDoubleTextBinding>()
            RightSideUiModel.SingleTextTwoIcon::class -> inflateViewBinding<WidgetRightSideSingleTextTwoIconBinding>()
            else -> throw IllegalArgumentException("add remaining type")
        }
    }

    // example todo
    /*fun setOnSwitchAction(onItemSwitchAction: (view: UiKitRightSideView, item: RightSideUiModel.Switcher) -> Unit) {
        (viewPool.findPoolOfViews(RightSideUiModel.Switcher::class).first as SwitcherBinding)
            .switch.setOnCheckedChangeListener{
                onItemSwitchAction(this, currentModel as RightSideUiModel.Switcher)
            }
    }*/

    fun bind(model: RightSideUiModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, model)
        when (model) {
            is RightSideUiModel.TwoLineText -> (pair.first as WidgetRightSideDoubleTextBinding).bind(model)
            is RightSideUiModel.SingleTextTwoIcon -> (pair.first as WidgetRightSideSingleTextTwoIconBinding).bind(model)
        }
        this.currentModel = model
    }

    private fun WidgetRightSideDoubleTextBinding.bind(model: RightSideUiModel.TwoLineText) {
        this.textViewFirst.bindOrGone(model.firstLineText)
        this.textViewSecond.bindOrGone(model.secondLineText)
    }

    private fun WidgetRightSideSingleTextTwoIconBinding.bind(model: RightSideUiModel.SingleTextTwoIcon) {
        this.textViewFirst.bindOrGone(model.text)
        this.imageViewFirstIcon.bindOrGone(model.firstIcon)
        this.imageViewSecondIcon.bindOrGone(model.secondIcon)
    }
}
