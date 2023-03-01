package org.p2p.uikit.components.right_side

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetRightSideDoubleTextBinding
import org.p2p.uikit.databinding.WidgetRightSideSingleTextBadgeBinding
import org.p2p.uikit.databinding.WidgetRightSideSingleTextTwoIconBinding
import org.p2p.uikit.utils.image.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.badgeRounded
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.viewpool.ComponentViewPool

class UiKitRightSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var currentModel: RightSideCellModel? = null
    private val viewPool = ComponentViewPool<RightSideCellModel>(this) {
        when (this) {
            RightSideCellModel.TwoLineText::class -> inflateViewBinding<WidgetRightSideDoubleTextBinding>()
            RightSideCellModel.SingleTextTwoIcon::class -> inflateViewBinding<WidgetRightSideSingleTextTwoIconBinding>()
            RightSideCellModel.TextBadge::class -> inflateViewBinding<WidgetRightSideSingleTextBadgeBinding>()
            else -> error("No type for viewPool: $this")
        }
    }

    val item: RightSideCellModel
        get() = currentModel ?: error("Not bind yet")

    init {
        if (isInEditMode) {
            inflateViewBinding<WidgetRightSideDoubleTextBinding>()
        }
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

    fun bind(model: RightSideCellModel) {
        val pair = viewPool.updatePoolOfViews(this.currentModel, model)
        when (model) {
            is RightSideCellModel.TwoLineText -> {
                (pair.first as WidgetRightSideDoubleTextBinding).bind(model)
            }
            is RightSideCellModel.SingleTextTwoIcon -> {
                (pair.first as WidgetRightSideSingleTextTwoIconBinding).bind(model)
            }
            is RightSideCellModel.TextBadge -> {
                (pair.first as WidgetRightSideSingleTextBadgeBinding).bind(model)
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

    private fun WidgetRightSideSingleTextBadgeBinding.bind(model: RightSideCellModel.TextBadge) {
        val textViewCellModel = TextViewCellModel.Raw(
            text = model.text,
            textColor = model.textColor,
            badgeBackground = TextViewBackgroundModel(badgeRounded(tint = model.badgeTint))
        )
        textViewText.bindOrGone(textViewCellModel)
    }
}
