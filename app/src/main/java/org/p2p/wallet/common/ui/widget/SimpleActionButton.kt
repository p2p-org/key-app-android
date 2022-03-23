package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetActionButtonSimpleBinding
import org.p2p.wallet.utils.dip

class SimpleActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val PADDING_HORIZONTAL_IN_DP = 12
        private const val PADDING_VERTICAL_IN_DP = 8
    }

    private val binding = WidgetActionButtonSimpleBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = HORIZONTAL

        val paddingHorizontal = dip(PADDING_HORIZONTAL_IN_DP)
        val paddingVertical = dip(PADDING_VERTICAL_IN_DP)
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleActionButton)

        val text = typedArray.getText(R.styleable.SimpleActionButton_actionButtonText)
        binding.actionTextView.text = text

        val imageResourceId = typedArray.getResourceId(R.styleable.SimpleActionButton_actionDrawable, 0)
        if (imageResourceId != 0) {
            binding.actionImageView.isVisible = true
            binding.actionImageView.setImageResource(imageResourceId)
        }
        typedArray.recycle()
    }
}
