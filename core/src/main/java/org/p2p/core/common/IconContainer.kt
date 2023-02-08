package org.p2p.core.common

import androidx.annotation.DrawableRes
import android.graphics.drawable.Drawable
import android.widget.ImageView

sealed interface IconContainer {

    data class Uri(
        val uri: String
    ) : IconContainer

    data class Res(
        @DrawableRes val drawableRes: Int
    ) : IconContainer

    data class DrawableIcon(
        val drawable: Drawable
    ) : IconContainer
}

fun ImageView.setIcon(icon: IconContainer): Unit = when (icon) {
    is IconContainer.DrawableIcon -> this.setImageDrawable(icon.drawable)
    is IconContainer.Res -> this.setImageResource(icon.drawableRes)
    is IconContainer.Uri -> {
        // todo static glide
    }
}