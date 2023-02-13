package org.p2p.uikit.components.finance_block

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetFinanceBlockBinding
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.toPx

class UiKitFinanceBlockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = inflateViewBinding<WidgetFinanceBlockBinding>()

    private var _item: FinanceBlockCellModel? = null
    val item: FinanceBlockCellModel
        get() = _item ?: throw IllegalStateException("Not bind yet")

    init {
        val paddingHorizontal = 16.toPx()
        val paddingVertical = 12.toPx()
        updatePadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        rippleForeground()
        minHeight = 72.toPx()
    }

    fun setOnClickAction(onItemClickAction: (view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit) {
        setOnClickListener {
            onItemClickAction.invoke(this, item)
        }
    }

    fun bind(model: FinanceBlockCellModel) {
        _item = model
        isEnabled = model.accessibility.isEnabled
        isClickable = model.accessibility.isClickable
        binding.leftSideView.isVisible = model.leftSideCellModel != null
        model.leftSideCellModel?.let { binding.leftSideView.bind(it) }
        binding.rightSideView.isVisible = model.rightSideCellModel != null
        model.rightSideCellModel?.let { binding.rightSideView.bind(it) }
    }
}
