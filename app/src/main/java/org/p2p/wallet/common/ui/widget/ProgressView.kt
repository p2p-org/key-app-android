package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetProgressViewBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetProgressViewBinding>()

    init {
        binding.root.setBackgroundColor(context.getColor(R.color.colorProgressBackground))
        binding.root.isClickable = true
        binding.root.isFocusable = true
    }
}
