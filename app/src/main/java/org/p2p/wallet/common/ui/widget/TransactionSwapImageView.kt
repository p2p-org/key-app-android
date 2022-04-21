package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.wallet.R
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.databinding.WidgetTransactionSwapImageBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE = 28

class TransactionSwapImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionSwapImageBinding>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionSwapImageView)

        val tokenIconSize = typedArray.getDimensionPixelSize(
            R.styleable.TransactionSwapImageView_tokenIconSize,
            resources.getDimensionPixelSize(R.dimen.history_transaction_image_token_icon_size)
        )

        with(binding) {
            sourceImageView.setViewSize(tokenIconSize)
            destinationImageView.setViewSize(tokenIconSize)
        }

        typedArray.recycle()
    }

    fun setSourceAndDestinationImages(
        glideManager: GlideManager,
        sourceIconUrl: String,
        destinationIconUrl: String
    ) {
        with(binding) {
            glideManager.apply {
                load(sourceImageView, sourceIconUrl, IMAGE_SIZE)
                load(destinationImageView, destinationIconUrl, IMAGE_SIZE)
            }
        }
    }

    private fun RoundedImageView.setViewSize(tokenIconSize: Int) {
        layoutParams = layoutParams.also { params ->
            params.height = tokenIconSize
            params.width = tokenIconSize
        }
    }
}
