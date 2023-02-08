package org.p2p.uikit.components.left_side

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.UiKitLeftSideIconTripleTextBinding
import org.p2p.uikit.model.bindOrGone
import org.p2p.uikit.utils.inflateViewBinding

class UiKitLeftSideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<UiKitLeftSideIconTripleTextBinding>()

    fun bind(model: LeftSideUiModel) {
        when (model) {
            is LeftSideUiModel.IconWithText -> binding.bind(model)
        }
    }

    private fun UiKitLeftSideIconTripleTextBinding.bind(model: LeftSideUiModel.IconWithText) {
        imageViewIcon.isVisible = model.icon != null
        model.icon?.let { imageViewIcon.bind(it) }

        textViewFirst.bindOrGone(model.firstLineText)
        textViewSecond.bindOrGone(model.secondLineText)
        textViewThird.bindOrGone(model.thirdLineText)
    }
}
