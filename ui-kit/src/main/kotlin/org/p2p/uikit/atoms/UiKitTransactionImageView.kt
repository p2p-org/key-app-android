package org.p2p.uikit.atoms

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetTransactionImageBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone

class UiKitTransactionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionImageBinding>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UiKitTransactionImageView)

        val statusIconSize = typedArray.getDimensionPixelSize(
            R.styleable.UiKitTransactionImageView_statusIconSize,
            resources.getDimensionPixelSize(R.dimen.ui_kit_transaction_image_icon_size)
        )

        with(binding.transactionStatus) {
            layoutParams = layoutParams.also {
                it.height = statusIconSize
                it.width = statusIconSize
            }
        }

        typedArray.recycle()
    }

    fun setTransactionIcon(@DrawableRes iconRes: Int) {
        binding.transactionTokenImageView.setImageResource(iconRes)
    }

    fun setStatusIcon(@DrawableRes statusIcon: Int?) {
        binding.transactionStatus.withImageOrGone(statusIcon)
    }
}
