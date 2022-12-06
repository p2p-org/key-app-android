package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.WidgetDoubleInputViewVerticalBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSendDetailsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: WidgetSendDetails = inflateViewBinding()


}
