package org.p2p.uikit.utils.text

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.view.Gravity
import android.widget.TextView
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.insets.InitialViewPadding
import org.p2p.uikit.utils.background.BackgroundUiModel
import org.p2p.uikit.utils.getColorStateList

data class TextViewUiModel(
    val text: TextContainer,
    @StyleRes val titleTextAppearance: Int? = null,
    @ColorRes val textColor: Int? = null,
    @Px val textSize: Float? = null,
    val gravity: Int = Gravity.CENTER,
    val background: TextViewBackgroundUiModel? = null
)

data class TextViewBackgroundUiModel(
    val drawable: BackgroundUiModel,
    val padding: InitialViewPadding = InitialViewPadding(0, 0, 0, 0)
)

fun TextView.bindOrGone(model: TextViewUiModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun TextView.bind(model: TextViewUiModel) {
    model.titleTextAppearance?.let { setTextAppearance(it) }
    model.textColor?.let { getColorStateList(it) }
    model.textSize?.let { textSize = it }
    gravity = model.gravity
    model.background?.let {
        background = it.drawable.background
        it.drawable.backgroundTint?.let { tint -> backgroundTintList = getColorStateList(tint) }
        it.padding.apply { updatePadding(left, top, right, bottom) }
    }
    model.text.applyTo(this)
}
