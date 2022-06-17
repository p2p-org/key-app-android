package org.p2p.wallet.common.extensions

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.content.Context
import android.graphics.drawable.Drawable

fun Context.getDrawableCompat(@DrawableRes drawableId: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableId)
