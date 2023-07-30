package org.p2p.uikit.components.edittext.v2

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.res.ColorStateList
import android.widget.TextView
import org.p2p.core.common.bindOrGone
import org.p2p.uikit.databinding.WidgetUiKitEditTextNewBinding
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes

internal class NewUiKitEditTextTipStateRenderer(private val binding: WidgetUiKitEditTextNewBinding) {
    private val textViewTip: TextView = binding.textViewTip
    private val containerInputView: ConstraintLayout = binding.containerInputView

    fun renderState(state: NewUiKitEditTextTipState) {
        textViewTip.setTextColorRes(state.tipColorRes)
        textViewTip.bindOrGone(state.tipText)

        containerInputView.backgroundTintList = ColorStateList.valueOf(binding.getColor(state.inputColor))
    }
}
