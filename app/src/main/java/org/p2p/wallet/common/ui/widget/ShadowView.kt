package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import org.p2p.wallet.R
import org.p2p.wallet.utils.toDp

class ShadowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var radius = 8f.toDp()
    private var shadowWidth = 2f.toDp()
    private var shadowRect = RectF(0f, 0f, 0f, 0f)
    private var backgroundRect = RectF(0f, 0f, 0f, 0f)

    private val shadowLayerPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            alpha = 255
            style = FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    init {
        var backgroundColor = Color.WHITE
        var shadowColor = backgroundColor

        context.obtainStyledAttributes(attrs, R.styleable.ShadowView).use { typedArray ->
            radius = typedArray.getDimension(R.styleable.ShadowView_radius, radius)
            shadowWidth = typedArray.getDimension(R.styleable.ShadowView_shadowWidth, 25f.toDp())
            backgroundColor = typedArray.getColor(R.styleable.ShadowView_backgroundColor, backgroundColor)
            shadowColor = typedArray.getColor(R.styleable.ShadowView_shadowColor, shadowColor)
        }

        shadowLayerPaint.color = backgroundColor
        shadowLayerPaint.setShadowLayer(shadowWidth, 0f, 0f, shadowColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shadowRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        backgroundRect = RectF(
            paddingLeft.toFloat() + shadowWidth,
            paddingTop + shadowWidth,
            w - paddingRight - shadowWidth,
            h - paddingBottom - shadowWidth
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        (this.parent as ViewGroup).clipChildren = false
        (this.parent as ViewGroup).clipToPadding = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(shadowRect, radius, radius, shadowLayerPaint)
    }

    fun setShadowColor(@ColorInt color: Int) {
        shadowLayerPaint.setShadowLayer(shadowWidth, 0f, 0f, color)
        invalidate()
    }

    fun setBackgroundViewColor(@ColorInt color: Int) {
        shadowLayerPaint.color = color
        invalidate()
    }
}
