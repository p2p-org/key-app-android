package org.p2p.uikit.utils

import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import android.widget.ImageView
import org.p2p.core.R
import org.p2p.core.glide.GlideManager
import org.p2p.core.glide.GlideManager.Companion.DEFAULT_IMAGE_SIZE

infix fun ImageView.withImageOrGone(@DrawableRes imageRes: Int?) {
    if (imageRes == null || imageRes == 0) {
        isVisible = false
    } else {
        isVisible = true
        setImageResource(imageRes)
    }
}

fun ImageView.loadUrl(
    glide: GlideManager,
    url: String?,
    size: Int = DEFAULT_IMAGE_SIZE,
    circleCrop: Boolean = false,
    placeholder: Int = R.drawable.ic_placeholder_v2
) {
    glide.load(
        imageView = this,
        url = url,
        size = size,
        circleCrop = circleCrop,
        placeholder = placeholder
    )
}
