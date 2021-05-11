package com.p2p.wallet.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.p2p.wallet.R
import com.p2p.wallet.databinding.WidgetProgressViewBinding

class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetProgressViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.colorProgressBackground))
        binding.root.isClickable = true
        binding.root.isFocusable = true
    }
}