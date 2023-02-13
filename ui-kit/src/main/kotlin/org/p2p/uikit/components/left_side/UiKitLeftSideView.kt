package org.p2p.uikit.components.left_side

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetLeftSideIconTripleTextBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.bindOrGone

class UiKitLeftSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetLeftSideIconTripleTextBinding>()

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
