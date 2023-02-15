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
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.toPx

data class TextViewCellModel(
    val text: TextContainer,
    @StyleRes val textAppearance: Int? = null,
    @ColorRes val textColor: Int? = null,
    val textSizeSp: Float? = null,
    val gravity: Int = Gravity.CENTER,
    val background: DrawableCellModel? = null,
    val padding: InitialViewPadding? = null,
) {
    constructor(
        text: TextContainer,
        @StyleRes textAppearance: Int? = null,
        @ColorRes textColor: Int? = null,
        textSizeSp: Float? = null,
        gravity: Int = Gravity.CENTER,
        badgeBackground: TextViewBackgroundModel? = null
    ) : this(
        text = text,
        textAppearance = textAppearance,
        textColor = textColor,
        textSizeSp = textSizeSp,
        gravity = gravity,
        background = badgeBackground?.background,
        padding = badgeBackground?.padding
    )
}

data class TextViewBackgroundModel(
    val background: DrawableCellModel = badgeRounded(),
    val padding: InitialViewPadding = badgePadding()
)

fun badgePadding(
    @Px left: Int = 8.toPx(),
    @Px top: Int = 0.toPx(),
    @Px right: Int = 8.toPx(),
    @Px bottom: Int = 0.toPx(),
): InitialViewPadding = InitialViewPadding(left, top, right, bottom)

fun badgeRounded(
    @Px cornerSize: Float = 32f.toPx(),
    @ColorRes tint: Int = R.color.elements_lime,
): DrawableCellModel = DrawableCellModel(
    drawable = shapeDrawable(shapeRoundedAll(cornerSize)),
    tint = tint,
)

fun TextView.bindOrGone(model: TextViewCellModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun TextView.bind(model: TextViewCellModel) {
    model.textAppearance?.let { setTextAppearance(it) }
    model.textColor?.let { setTextColor(getColorStateList(it)) }
    model.textSizeSp?.let { textSize = it }
    gravity = model.gravity
    model.background?.applyBackground(this)
    updatePadding(
        left = model.padding?.left.orZero(),
        top = model.padding?.top.orZero(),
        right = model.padding?.right.orZero(),
        bottom = model.padding?.bottom.orZero(),
    )
    model.text.applyTo(this)
}
