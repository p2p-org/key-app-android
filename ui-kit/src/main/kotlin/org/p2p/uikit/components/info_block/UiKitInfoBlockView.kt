package org.p2p.uikit.components.info_block

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetInfoBlockBinding
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.toPx

class UiKitInfoBlockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetInfoBlockBinding>()

    private var _item: InfoBlockCellModel? = null
    val item: InfoBlockCellModel
        get() = _item ?: error("Method ::bind was not called")

    init {
        val paddingHorizontal = 16.toPx()
        val paddingVertical = 12.toPx()
        updatePadding(
            left = paddingHorizontal,
            top = paddingVertical,
            right = paddingHorizontal,
            bottom = paddingVertical
        )
        rippleForeground()
    }

    fun setOnClickAction(onItemClickAction: (view: UiKitInfoBlockView, item: InfoBlockCellModel) -> Unit) {
        setOnClickListener {
            onItemClickAction.invoke(this, item)
        }
    }

    fun bind(model: InfoBlockCellModel) = with(binding) {
        _item = model
        model.background?.applyBackground(this.root)
        imageViewIcon.bindOrGone(model.icon)
        textViewFirst.bindOrGone(model.firstLineText)
        textViewSecond.bindOrGone(model.secondLineText)
    }
}
