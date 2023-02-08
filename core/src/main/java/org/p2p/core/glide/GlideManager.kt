package org.p2p.core.glide

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.R

private const val DEFAULT_IMAGE_SIZE = 56

class GlideManager(context: Context) {

    private val requestBuilder: RequestBuilder<PictureDrawable> by lazy {
        Glide.with(context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())
    }

    fun load(imageView: ImageView, url: String?, size: Int = DEFAULT_IMAGE_SIZE, circleCrop: Boolean = false) {
        if (url.isNullOrEmpty()) return

        if (url.contains(".svg")) {
            requestBuilder
                .load(Uri.parse(url))
                .apply(RequestOptions().override(size, size))
                .centerCrop()
                .into(imageView)
        } else {
            Glide
                .with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_image).apply {
                    val target = if (circleCrop) {
                        circleCrop()
                    } else this
                    target.into(imageView)
                }
        }
    }

    fun ImageView.load(url: String) {
        if (url.contains(".svg")) {
            requestBuilder
                .load(Uri.parse(url))
                .apply(RequestOptions().override(DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE))
                .centerCrop()
                .into(this)
        } else {
            Glide.with(this).load(url).into(this)
        }
    }
}
