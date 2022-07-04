package org.p2p.uikit.atoms

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import org.p2p.uikit.R
import org.p2p.uikit.utils.toPx

private const val RADIUS_LIST_SIZE = 8
private const val VERTICAL_PADDING_DP = 0
private const val HORIZONTAL_PADDING_DP = 8

class UiKitBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class Shape {
        OVAL,
        RECTANGLE
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitBadgeView).use { typedArray ->
            val defaultTextColor = ContextCompat.getColor(context, R.color.mountain)
            val hasStroke = typedArray.getBoolean(R.styleable.UiKitBadgeView_hasStroke, false)
            val shape = when (typedArray.getInt(R.styleable.UiKitBadgeView_shape, 0)) {
                0 -> Shape.OVAL
                else -> Shape.RECTANGLE
            }

            setBackground(shape, hasStroke)
            setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text4)
            setTextColor(typedArray.getColor(R.styleable.UiKitBadgeView_android_textColor, defaultTextColor))
        }

        gravity = Gravity.CENTER
        setPadding(
            HORIZONTAL_PADDING_DP.toPx(),
            VERTICAL_PADDING_DP.toPx(),
            HORIZONTAL_PADDING_DP.toPx(),
            VERTICAL_PADDING_DP.toPx()
        )
    }

    private fun setBackground(shape: Shape, hasStroke: Boolean) {
        val ovalCornerRadius = resources.getDimension(R.dimen.ui_kit_badge_oval_corner_radius)
        val rectangleCornerRadius = resources.getDimension(R.dimen.ui_kit_badge_rectangle_corner_radius)
        val cornerRadiusDp = if (shape == Shape.OVAL) ovalCornerRadius else rectangleCornerRadius
        val strokeBackgroundId = if (shape == Shape.OVAL) {
            R.drawable.backgroun_badge_stroke_oval
        } else {
            R.drawable.backgroun_badge_stroke_rectangle
        }
        val strokeBackground = ContextCompat.getDrawable(context, strokeBackgroundId)
        val backgroundColor = getBackgroundColor()
        val filledBackground = createFilledBackground(backgroundColor, cornerRadiusDp)
        background = if (hasStroke) strokeBackground else filledBackground
    }

    private fun getBackgroundColor(): Int {
        return (background as? ColorDrawable)?.color ?: ContextCompat.getColor(context, R.color.lime)
    }

    private fun createFilledBackground(
        backgroundColor: Int,
        cornerRadiusDp: Float,
    ): LayerDrawable {
        val cornerRadiusPx = cornerRadiusDp.toPx()
        val corners = FloatArray(RADIUS_LIST_SIZE) { cornerRadiusPx }

        val backgroundShape = ShapeDrawable().apply {
            shape = RoundRectShape(corners, null, null)
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL
        }

        val layers = arrayOf(backgroundShape)

        return LayerDrawable(layers)
    }
}
