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
import org.p2p.core.utils.orZero
import org.p2p.uikit.utils.background.DrawableUiModel
import org.p2p.uikit.utils.background.applyBackground
import org.p2p.uikit.utils.getColorStateList

data class TextViewUiModel(
    val text: TextContainer,
    @StyleRes val titleTextAppearance: Int? = null,
    @ColorRes val textColor: Int? = null,
    @Px val textSize: Float? = null,
    val gravity: Int = Gravity.CENTER,
    val badgeBackground: TextViewBackgroundUiModel? = null
)

data class TextViewBackgroundUiModel(
    val background: DrawableUiModel,
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
    model.badgeBackground?.background?.applyBackground(this)
    updatePadding(
        left = model.badgeBackground?.padding?.left.orZero(),
        top = model.badgeBackground?.padding?.top.orZero(),
        right = model.badgeBackground?.padding?.right.orZero(),
        bottom = model.badgeBackground?.padding?.bottom.orZero(),
    )
    model.text.applyTo(this)
}
