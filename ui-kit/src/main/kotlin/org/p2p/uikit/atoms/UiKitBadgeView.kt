package org.p2p.uikit.atoms

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import org.p2p.uikit.R
import org.p2p.uikit.utils.toPx

private const val RADII_LIST_SIZE = 8
private const val VERTICAL_PADDING_DP = 0
private const val HORIZONTAL_PADDING_DP = 8

class UiKitBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class Shape(
        @DrawableRes val strokeBackgroundRes: Int,
        @DimenRes val cornerRadiusDimesRes: Int
    ) {
        OVAL(
            R.drawable.background_badge_stroke_oval,
            R.dimen.ui_kit_badge_oval_corner_radius
        ),
        RECTANGLE(
            R.drawable.background_badge_stroke_rectangle,
            R.dimen.ui_kit_badge_rectangle_corner_radius
        );

        companion object {
            fun valueOf(value: Int): Shape {
                require(value in values().indices) { "Value $value must be in ${values().indices}" }
                return values()[value]
            }
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitBadgeView).use { typedArray ->
            val defaultTextColor = context.getColor(R.color.mountain)
            val hasStroke = typedArray.getBoolean(R.styleable.UiKitBadgeView_hasStroke, false)
            val shape = Shape.valueOf(typedArray.getInt(R.styleable.UiKitBadgeView_shape, Shape.OVAL.ordinal))

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
        val strokeBackground = ContextCompat.getDrawable(context, shape.strokeBackgroundRes)
        val backgroundColor = getBackgroundColor()
        val filledBackground = createFilledBackground(backgroundColor, shape.cornerRadiusDimesRes)

        background = if (hasStroke) strokeBackground else filledBackground
    }

    private fun getBackgroundColor(): Int {
        return (background as? ColorDrawable)?.color ?: context.getColor(R.color.lime)
    }

    private fun createFilledBackground(
        @ColorInt backgroundColor: Int,
        @DimenRes cornerRadiusDimenRes: Int,
    ): LayerDrawable {
        val cornerRadiusPx = resources.getDimension(cornerRadiusDimenRes).toPx()
        val corners = FloatArray(RADII_LIST_SIZE) { cornerRadiusPx }

        val backgroundShape = ShapeDrawable().apply {
            shape = RoundRectShape(corners, null, null)
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL
        }

        val layers = arrayOf(backgroundShape)

        return LayerDrawable(layers)
    }
}
