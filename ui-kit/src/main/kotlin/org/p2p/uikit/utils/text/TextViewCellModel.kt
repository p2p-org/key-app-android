package org.p2p.uikit.utils.text

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.widget.TextView
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.insets.InitialViewPadding
import org.p2p.core.utils.orZero
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.skeleton.SkeletonDrawable
import org.p2p.uikit.utils.skeleton.bindSkeleton
import org.p2p.uikit.utils.toPx

sealed interface TextViewCellModel {

    data class Raw(
        val text: TextContainer,
        @StyleRes val textAppearance: Int? = null,
        @ColorRes val textColor: Int? = null,
        val textSize: TextViewSize? = null,
        val gravity: Int? = null,
        val badgeBackground: TextViewBackgroundModel? = null,
        val autoSizeConfiguration: TextViewAutoSizeConfiguration? = null,
        val maxLines: Int? = null,
        val ellipsize: TextUtils.TruncateAt? = null
    ) : TextViewCellModel

    data class Skeleton(
        val skeleton: SkeletonCellModel,
    ) : TextViewCellModel
}

data class TextViewSize(
    val textSize: Float,
    val typedValue: Int = TypedValue.COMPLEX_UNIT_SP
)

// Default minimum size for auto-sizing text in scaled pixels.
private const val DEFAULT_AUTO_SIZE_MIN_TEXT_SIZE_IN_SP = 12

// Default maximum size for auto-sizing text in scaled pixels.
private const val DEFAULT_AUTO_SIZE_MAX_TEXT_SIZE_IN_SP = 112

// Default value for the step size in pixels.
private const val DEFAULT_AUTO_SIZE_GRANULARITY_IN_PX = 1

data class TextViewAutoSizeConfiguration(
    val autoSizeMinTextSize: Int = DEFAULT_AUTO_SIZE_MIN_TEXT_SIZE_IN_SP,
    val autoSizeMaxTextSize: Int = DEFAULT_AUTO_SIZE_MAX_TEXT_SIZE_IN_SP,
    val autoSizeStepGranularity: Int = DEFAULT_AUTO_SIZE_GRANULARITY_IN_PX,
    val typedValue: Int = TypedValue.COMPLEX_UNIT_SP,
    val autoSizeTextType: Int = TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM,
)

data class TextViewBackgroundModel(
    val background: DrawableCellModel = badgeRounded(),
    val padding: InitialViewPadding = badgePadding()
)

fun badgePadding(
    @Px left: Int = 8.toPx(),
    @Px top: Int = 1.toPx(),
    @Px right: Int = 8.toPx(),
    @Px bottom: Int = 3.toPx(),
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

fun TextView.bindOrInvisible(model: TextViewCellModel?) {
    this.isInvisible = model == null
    if (model != null) bind(model)
}

fun TextView.bind(model: TextViewCellModel) {
    if (equalsNewCellModel(model)) return
    when (model) {
        is TextViewCellModel.Raw -> bind(model)
        is TextViewCellModel.Skeleton -> bindSkeleton(model)
    }
}

fun TextView.bind(model: TextViewCellModel.Raw) {
    val initialTextStyle = saveAndGetInitialTextStyle()
    model.textAppearance?.let { setTextAppearance(it) }
        ?: kotlin.run {
            typeface = initialTextStyle.typeface
            letterSpacing = initialTextStyle.letterSpacing
        }
    model.textColor?.let { setTextColor(getColorStateList(it)) }
        ?: kotlin.run { setTextColor(initialTextStyle.textColors) }

    model.textSize?.let { setTextSize(it.typedValue, it.textSize) }
        ?: kotlin.run { setTextSize(TypedValue.COMPLEX_UNIT_PX, initialTextStyle.textSize) }
    gravity = model.gravity ?: initialTextStyle.gravity
    model.badgeBackground?.background?.applyBackground(this)
        ?: kotlin.run {
            background = initialTextStyle.background
            backgroundTintList = initialTextStyle.backgroundTint
        }
    setHintTextColor(initialTextStyle.hintTextColors)
    if (foreground is SkeletonDrawable) {
        foreground = null
    }

    maxLines = model.maxLines ?: initialTextStyle.maxLines
    ellipsize = model.ellipsize ?: initialTextStyle.ellipsize

    val autoSize = model.autoSizeConfiguration
    val initialAutoSize = initialTextStyle.textViewAutoSizeConfiguration
    val autoSizeTextType =
        (autoSize?.autoSizeTextType ?: initialAutoSize.autoSizeTextType)
    setAutoSizeTextTypeWithDefaults(autoSizeTextType)
    if (autoSizeTextType != TextView.AUTO_SIZE_TEXT_TYPE_NONE) {
        setAutoSizeTextTypeUniformWithConfiguration(
            autoSize?.autoSizeMinTextSize ?: initialAutoSize.autoSizeMinTextSize,
            autoSize?.autoSizeMaxTextSize ?: initialAutoSize.autoSizeMaxTextSize,
            autoSize?.autoSizeStepGranularity ?: initialAutoSize.autoSizeStepGranularity,
            autoSize?.typedValue ?: initialAutoSize.typedValue,
        )
    }

    updatePadding(
        left = model.badgeBackground?.padding?.left.orZero(),
        top = model.badgeBackground?.padding?.top.orZero(),
        right = model.badgeBackground?.padding?.right.orZero(),
        bottom = model.badgeBackground?.padding?.bottom.orZero(),
    )
    bind(model.text)
}

fun TextView.bindSkeleton(model: TextViewCellModel.Skeleton) {
    saveAndGetInitialTextStyle()
    val transparent = context.getColorStateList(android.R.color.transparent)
    setTextColor(transparent)
    setHintTextColor(transparent)
    text = ""
    bindSkeleton(model.skeleton)
}

private fun TextView.saveAndGetInitialTextStyle(): InitialTextStyle {
    val tagKey = R.id.initial_text_style_tag_id
    return getTag(tagKey) as? InitialTextStyle ?: let {
        val initialTextStyle = InitialTextStyle(this)
        setTag(tagKey, initialTextStyle)
        initialTextStyle
    }
}

private fun TextView.equalsNewCellModel(newModel: TextViewCellModel): Boolean {
    val previewModel = previewTextViewCellModel()
    val isEquals = previewModel != null &&
        newModel.hashCode() == previewModel.hashCode() &&
        previewModel == newModel
    if (!isEquals) {
        setTag(R.id.preview_text_cell_model_tag_id, newModel)
    }
    return isEquals
}

private fun TextView.previewTextViewCellModel(): TextViewCellModel? {
    val tagKey = R.id.preview_text_cell_model_tag_id
    return getTag(tagKey) as? TextViewCellModel
}

private data class InitialTextStyle(
    @Px val textSize: Float,
    val textColors: ColorStateList?,
    val hintTextColors: ColorStateList?,
    val letterSpacing: Float,
    val typeface: Typeface?,
    val background: Drawable?,
    val backgroundTint: ColorStateList?,
    val gravity: Int,
    val textViewAutoSizeConfiguration: TextViewAutoSizeConfiguration,
    val maxLines: Int,
    val ellipsize: TextUtils.TruncateAt?
) {
    constructor(textView: TextView) : this(
        textSize = textView.textSize,
        letterSpacing = textView.letterSpacing,
        textColors = textView.textColors,
        typeface = textView.typeface,
        background = textView.background,
        backgroundTint = textView.backgroundTintList,
        gravity = textView.gravity,
        hintTextColors = textView.hintTextColors,
        maxLines = textView.maxLines,
        ellipsize = textView.ellipsize,
        textViewAutoSizeConfiguration = TextViewAutoSizeConfiguration(
            autoSizeTextType = textView.autoSizeTextType,
            autoSizeMinTextSize = textView.autoSizeMinTextSize,
            autoSizeMaxTextSize = textView.autoSizeMaxTextSize,
            autoSizeStepGranularity = textView.autoSizeStepGranularity,
            typedValue = TypedValue.COMPLEX_UNIT_PX,
        ),
    )
}
