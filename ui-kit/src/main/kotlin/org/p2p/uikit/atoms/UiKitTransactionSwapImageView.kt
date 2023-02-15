package org.p2p.uikit.atoms

import androidx.annotation.DrawableRes
import androidx.core.content.res.use
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetTransactionSwapImageBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone

private const val IMAGE_SIZE = 28

class TransactionSwapImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionSwapImageBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitTransactionSwapImageView).use { typedArray ->
            val tokenIconSize = typedArray.getDimensionPixelSize(
                R.styleable.UiKitTransactionSwapImageView_tokenIconSize,
                resources.getDimensionPixelSize(R.dimen.ui_kit_transaction_image_token_icon_size)
            )

            with(binding) {
                sourceImageView.setViewSize(tokenIconSize)
                destinationImageView.setViewSize(tokenIconSize)
            }
        }
    }

    fun setSourceAndDestinationImages(
        glideManager: GlideManager,
        sourceIconUrl: String?,
        destinationIconUrl: String?
    ) {
        with(binding) {
            glideManager.apply {
                load(sourceImageView, sourceIconUrl, IMAGE_SIZE)
                load(destinationImageView, destinationIconUrl, IMAGE_SIZE)
            }
        }
    }

    private fun UiKitRoundedImageView.setViewSize(tokenIconSize: Int) {
        layoutParams = layoutParams.also { params ->
            params.height = tokenIconSize
            params.width = tokenIconSize
        }
    }

    fun setStatusIcon(@DrawableRes statusIcon: Int?) {
        binding.transactionStatus.withImageOrGone(statusIcon)
    }
}
