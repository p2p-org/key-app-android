package com.p2p.wallet.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.databinding.WidgetDoubleTextViewBinding

class DoubleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetDoubleTextViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleTextView)

        val topText = typedArray.getText(R.styleable.DoubleTextView_topText)
        binding.topTextView.text = topText

        val bottomText = typedArray.getText(R.styleable.DoubleTextView_bottomText)
        binding.bottomTextView.text = bottomText

        val endImageResourceId = typedArray.getResourceId(R.styleable.DoubleTextView_drawableEnd, 0)
        if (endImageResourceId != 0) {
            binding.iconImageView.isVisible = true
            binding.iconImageView.setImageResource(endImageResourceId)
        }
        typedArray.recycle()
    }

    fun setTopText(text: String) {
        binding.topTextView.text = text
    }

    fun setBottomText(text: String) {
        binding.bottomTextView.text = text
    }
}