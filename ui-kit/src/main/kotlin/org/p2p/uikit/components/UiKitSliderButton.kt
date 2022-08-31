package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.uikit.databinding.WidgetSliderButtonBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSliderButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetSliderButtonBinding>()

    init {

    }
}
