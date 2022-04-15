package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.WidgetTransactionSwapImageBinding
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.viewbinding.context

private const val IMAGE_SIZE = 29

class TransactionSwapImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val requestBuilder: RequestBuilder<PictureDrawable> =
        Glide.with(context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())

    private val binding = WidgetTransactionSwapImageBinding.inflate(
        LayoutInflater.from(context), this
    )

    fun setSourceAndDestinationImages(sourceIconUrl: String, destinationIconUrl: String) {
        with(binding) {
            loadImage(sourceImageView, sourceIconUrl)
            loadImage(destinationImageView, destinationIconUrl)
        }
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (".svg" in url) {
            val size = binding.context.dip(IMAGE_SIZE)
            requestBuilder.load(url)
                .apply(RequestOptions().override(size, size))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
