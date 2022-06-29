package org.p2p.uikit.atoms.cells

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetAmountViewBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withTextOrGone

class UiKitAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var usdAmount: String? = null
        set(value) {
            binding.usdAmountTextView.withTextOrGone(value)
            field = value
        }

    var tokenAmount: String? = null
        set(value) {
            binding.tokenAmountTextView.withTextOrGone(value)
            field = value
        }

    private val binding = inflateViewBinding<WidgetAmountViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitAmountView).use { typedArray ->
            usdAmount = typedArray.getString(R.styleable.UiKitAmountView_usdAmount)
            tokenAmount = typedArray.getString(R.styleable.UiKitAmountView_tokenAmount)
        }
    }
}
