package org.p2p.uikit.components.finance_block

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.components.left_side.UiKitLeftSideView
import org.p2p.uikit.components.right_side.UiKitRightSideView
import org.p2p.uikit.databinding.WidgetMainCellBinding
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.applyForeground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setMargins
import org.p2p.uikit.utils.toPx

class UiKitMainCellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetMainCellBinding>()

    val leftSideView: UiKitLeftSideView
        get() = binding.leftSideView
    val rightSideView: UiKitRightSideView
        get() = binding.rightSideView

    private var _item: MainCellModel? = null
    val item: MainCellModel
        get() = _item ?: error("Method ::bind was not called")

    private var styleType = MainCellStyle.FINANCE_BLOCK

    init {
        val paddingHorizontal = 16.toPx()
        val paddingVertical = 12.toPx()
        updatePadding(
            left = paddingHorizontal,
            top = paddingVertical,
            right = paddingHorizontal,
            bottom = paddingVertical
        )
        if (!isInEditMode) {
            // editor preview crashes when trying to apply rippleForeground
            rippleForeground()
        }
        bindViewStyle(styleType)
    }

    fun setOnClickAction(onItemClickAction: (view: UiKitMainCellView, item: MainCellModel) -> Unit) {
        setOnClickListener {
            onItemClickAction.invoke(this, item)
        }
    }

    fun setOnRightFirstTextClickListener(onFirstRightTextClicked: () -> Unit) {
        rightSideView.setOnFirstTextClickAction(onFirstRightTextClicked)
    }

    fun bindViewStyle(style: MainCellStyle) {
        styleType = style
        minHeight = when (style) {
            MainCellStyle.FINANCE_BLOCK -> 72.toPx()
            MainCellStyle.BASE_CELL -> 64.toPx()
        }
        leftSideView.bindViewStyle(style)
        rightSideView.bindViewStyle(style)
    }

    fun bind(model: MainCellModel) = with(binding) {
        _item = model
        setMargins(left = model.horizontalMargins, right = model.horizontalMargins)
        isEnabled = model.accessibility.isEnabled
        isClickable = model.accessibility.isClickable
        model.background?.applyBackground(this.root)
        model.foreground.applyForeground(this.root)
        leftSideView.isVisible = model.leftSideCellModel != null
        model.leftSideCellModel?.let(leftSideView::bind)
        rightSideView.isVisible = model.rightSideCellModel != null
        model.rightSideCellModel?.let(rightSideView::bind)
    }
}
