package org.p2p.uikit.utils.skeleton

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.shapes.Shape
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.tan

class SkeletonDrawable : PaintDrawable() {
    private val mUpdateListener = AnimatorUpdateListener { invalidateSelf() }
    private val mShimmerPaint = paint
    private val mDrawRect = Rect()
    private val mShaderMatrix = Matrix()
    private var mValueAnimator: ValueAnimator? = null
    private var mShimmer: Shimmer? = null

    init {
        mShimmerPaint.isAntiAlias = true
    }

    fun setShimmer(shimmer: Shimmer) {
        mShimmer = shimmer
        updateShader()
        updateValueAnimator()
        invalidateSelf()
    }

    /**
     * Starts the shimmer animation.
     */
    fun startShimmer() {
        if (mValueAnimator != null && !isShimmerStarted && callback != null) {
            mValueAnimator!!.start()
        }
    }

    /**
     * Stops the shimmer animation.
     */
    fun stopShimmer() {
        if (mValueAnimator != null && isShimmerStarted) {
            mValueAnimator!!.cancel()
        }
    }

    /**
     * Return whether the shimmer animation has been started.
     */
    val isShimmerStarted: Boolean
        get() = mValueAnimator != null && mValueAnimator!!.isStarted

    public override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val width = bounds.width()
        val height = bounds.height()
        mDrawRect[0, 0, width] = height
        updateShader()
        maybeStartShimmer()
    }

    override fun onDraw(shape: Shape, canvas: Canvas, paint: Paint) {
        if (mShimmer == null || mShimmerPaint.shader == null) {
            return
        }
        val tiltTan = tan(Math.toRadians(mShimmer!!.tilt.toDouble())).toFloat()
        val translateHeight = mDrawRect.height() + tiltTan * mDrawRect.width()
        val translateWidth = mDrawRect.width() + tiltTan * mDrawRect.height()
        val dx: Float
        val dy: Float
        val animatedValue = if (mValueAnimator != null) mValueAnimator!!.animatedFraction else 0f
        when (mShimmer!!.direction) {
            Shimmer.Direction.LEFT_TO_RIGHT -> {
                dx = offset(-translateWidth, translateWidth, animatedValue)
                dy = 0f
            }
            Shimmer.Direction.RIGHT_TO_LEFT -> {
                dx = offset(translateWidth, -translateWidth, animatedValue)
                dy = 0f
            }
            Shimmer.Direction.TOP_TO_BOTTOM -> {
                dx = 0f
                dy = offset(-translateHeight, translateHeight, animatedValue)
            }
            Shimmer.Direction.BOTTOM_TO_TOP -> {
                dx = 0f
                dy = offset(translateHeight, -translateHeight, animatedValue)
            }
            else -> {
                dx = offset(-translateWidth, translateWidth, animatedValue)
                dy = 0f
            }
        }
        mShaderMatrix.reset()
        mShaderMatrix.setRotate(mShimmer!!.tilt, mDrawRect.width() / 2f, mDrawRect.height() / 2f)
        mShaderMatrix.postTranslate(dx, dy)
        mShimmerPaint.shader.setLocalMatrix(mShaderMatrix)
        shape.draw(canvas, mShimmerPaint)
    }

    override fun setAlpha(alpha: Int) {
        // No-op, modify the Shimmer object you pass in instead
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // No-op, modify the Shimmer object you pass in instead
    }

    override fun getOpacity(): Int {
        return if (mShimmer != null && (mShimmer!!.clipToChildren || mShimmer!!.alphaShimmer)) {
            PixelFormat.TRANSLUCENT
        } else {
            PixelFormat.OPAQUE
        }
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    private fun updateValueAnimator() {
        if (mShimmer == null) {
            return
        }
        val started: Boolean
        if (mValueAnimator != null) {
            started = mValueAnimator!!.isStarted
            mValueAnimator!!.cancel()
            mValueAnimator!!.removeAllUpdateListeners()
        } else {
            started = false
        }
        mValueAnimator =
            ValueAnimator.ofFloat(0f, 1f + (mShimmer!!.repeatDelay / mShimmer!!.animationDuration).toFloat())
                .apply {
                    repeatMode = mShimmer!!.repeatMode
                    repeatCount = mShimmer!!.repeatCount
                    duration = mShimmer!!.animationDuration + mShimmer!!.repeatDelay
                    addUpdateListener(mUpdateListener)
                    if (started) {
                        start()
                    }
                }
    }

    fun maybeStartShimmer() {
        if (mValueAnimator != null &&
            !mValueAnimator!!.isStarted &&
            mShimmer != null &&
            mShimmer!!.autoStart &&
            callback != null
        ) {
            mValueAnimator!!.start()
        }
    }

    private fun updateShader() {
        val bounds = bounds
        val boundsWidth = bounds.width()
        val boundsHeight = bounds.height()
        if (boundsWidth == 0 || boundsHeight == 0 || mShimmer == null) {
            return
        }
        val width = mShimmer!!.width(boundsWidth)
        val height = mShimmer!!.height(boundsHeight)
        val shader: Shader
        shader = when (mShimmer!!.shape) {
            Shimmer.Shape.LINEAR -> {
                val vertical = (
                    mShimmer!!.direction == Shimmer.Direction.TOP_TO_BOTTOM ||
                        mShimmer!!.direction == Shimmer.Direction.BOTTOM_TO_TOP
                    )
                val endX = if (vertical) 0 else width
                val endY = if (vertical) height else 0
                LinearGradient(
                    0f,
                    0f,
                    endX.toFloat(),
                    endY.toFloat(),
                    mShimmer!!.colors,
                    mShimmer!!.positions,
                    Shader.TileMode.CLAMP
                )
            }
            Shimmer.Shape.RADIAL -> RadialGradient(
                width / 2f,
                height / 2f, (max(width, height) / sqrt(2.0)).toFloat(),
                mShimmer!!.colors,
                mShimmer!!.positions,
                Shader.TileMode.CLAMP
            )
            else -> {
                val vertical = (
                    mShimmer!!.direction == Shimmer.Direction.TOP_TO_BOTTOM ||
                        mShimmer!!.direction == Shimmer.Direction.BOTTOM_TO_TOP
                    )
                val endX = if (vertical) 0 else width
                val endY = if (vertical) height else 0
                LinearGradient(
                    0f,
                    0f,
                    endX.toFloat(),
                    endY.toFloat(),
                    mShimmer!!.colors,
                    mShimmer!!.positions,
                    Shader.TileMode.CLAMP
                )
            }
        }
        mShimmerPaint.shader = shader
    }
}
