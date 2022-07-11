package org.p2p.uikit.utils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible

infix fun ImageView.withImageOrGone(@DrawableRes imageRes: Int?) {
    if (imageRes == null || imageRes == 0) {
        isVisible = false
    } else {
        isVisible = true
        setImageResource(imageRes)
    }
}
