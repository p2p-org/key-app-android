package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.databinding.WidgetTransactionSwapImageBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE = 29

class TransactionSwapImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionSwapImageBinding>()

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
}
