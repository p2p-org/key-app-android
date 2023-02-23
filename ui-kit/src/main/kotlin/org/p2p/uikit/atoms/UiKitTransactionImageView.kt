package org.p2p.uikit.atoms

import androidx.annotation.DrawableRes
import androidx.core.content.res.use
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetTransactionImageBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone

private const val IMAGE_SIZE = 48

@Deprecated(message = "use UiKitIconWrapper")
class UiKitTransactionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionImageBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitTransactionImageView).use { typedArray ->
            val statusIconSize = typedArray.getDimensionPixelSize(
                R.styleable.UiKitTransactionImageView_statusIconSize,
                resources.getDimensionPixelSize(R.dimen.ui_kit_transaction_image_icon_size)
            )

            with(binding.imageViewStatus) {
                layoutParams = layoutParams.also {
                    it.height = statusIconSize
                    it.width = statusIconSize
                }
            }
        }
    }

    fun setTransactionIcon(@DrawableRes iconRes: Int) = with(binding) {
        imageViewToken.scaleType = ImageView.ScaleType.CENTER
        imageViewToken.setImageResource(iconRes)
    }

    fun setTokenImage(
        glideManager: GlideManager,
        tokenIconUrl: String
    ) = with(binding) {
        imageViewToken.scaleType = ImageView.ScaleType.FIT_CENTER
        glideManager.load(imageViewToken, tokenIconUrl, IMAGE_SIZE, circleCrop = true)
    }

    fun setStatusIcon(@DrawableRes statusIcon: Int?) {
        binding.imageViewStatus.withImageOrGone(statusIcon)
    }
}
