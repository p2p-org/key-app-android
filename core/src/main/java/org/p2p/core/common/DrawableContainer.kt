package org.p2p.core.common

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import kotlinx.parcelize.Parcelize
import org.p2p.core.glide.SvgSoftwareLayerSetter

sealed class DrawableContainer : Parcelable {

    companion object {
        operator fun invoke(@DrawableRes textRes: Int) =
            Res(textRes)

        operator fun invoke(iconUrl: String) =
            Raw(iconUrl)
    }

    abstract fun applyTo(imageView: ImageView)

    @Parcelize
    class Res(@DrawableRes private val drawableRes: Int) : DrawableContainer() {
        override fun applyTo(imageView: ImageView) {
            imageView.setImageResource(drawableRes)
        }
    }

    @Parcelize
    class Raw(
        private val iconUrl: String
    ) : DrawableContainer() {
        override fun applyTo(imageView: ImageView) {
            val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(imageView.context)
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())

            if (iconUrl.contains(".svg")) {
                requestBuilder
                    .load(Uri.parse(iconUrl))
                    .apply(RequestOptions().override(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE))
                    .centerCrop()
                    .into(imageView)
            } else {
                Glide.with(imageView).load(iconUrl).into(imageView)
            }
        }
    }
}
