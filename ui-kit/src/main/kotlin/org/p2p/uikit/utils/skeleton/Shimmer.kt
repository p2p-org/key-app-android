/**
 * Copyright (c) 2015-present, Facebook, Inc. All rights reserved.
 *
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package org.p2p.uikit.utils.skeleton

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.Px
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

class Shimmer {
    /** The shape of the shimmer's highlight. By default LINEAR is used.  */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Shape.LINEAR, Shape.RADIAL)
    annotation class Shape {
        companion object {
            /** Linear gives a ray reflection effect.  */
            const val LINEAR = 0

            /** Radial gives a spotlight effect.  */
            const val RADIAL = 1
        }
    }

    /** Direction of the shimmer's sweep.  */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Direction.LEFT_TO_RIGHT, Direction.TOP_TO_BOTTOM, Direction.RIGHT_TO_LEFT, Direction.BOTTOM_TO_TOP)
    annotation class Direction {
        companion object {
            const val LEFT_TO_RIGHT = 0
            const val TOP_TO_BOTTOM = 1
            const val RIGHT_TO_LEFT = 2
            const val BOTTOM_TO_TOP = 3
        }
    }

    val positions = FloatArray(COMPONENT_COUNT)
    val colors = IntArray(COMPONENT_COUNT)
    val bounds = RectF()

    @Direction
    var direction = Direction.LEFT_TO_RIGHT

    @ColorInt
    var highlightColor = Color.WHITE

    @ColorInt
    var baseColor = 0x4cffffff

    @Shape
    var shape = Shape.LINEAR
    var fixedWidth = 0
    var fixedHeight = 0
    var widthRatio = 1f
    var heightRatio = 1f
    var intensity = 0f
    var dropoff = 0.5f
    var tilt = 20f
    var clipToChildren = true
    var autoStart = true
    var alphaShimmer = true
    var repeatCount = ValueAnimator.INFINITE
    var repeatMode = ValueAnimator.RESTART
    var animationDuration = 1000L
    var repeatDelay: Long = 0
    fun width(width: Int): Int {
        return if (fixedWidth > 0) fixedWidth else (widthRatio * width).roundToInt()
    }

    fun height(height: Int): Int {
        return if (fixedHeight > 0) fixedHeight else (heightRatio * height).roundToInt()
    }

    fun updateColors() {
        when (shape) {
            Shape.LINEAR -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }
            Shape.RADIAL -> {
                colors[0] = highlightColor
                colors[1] = highlightColor
                colors[2] = baseColor
                colors[3] = baseColor
            }
            else -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }
        }
    }

    fun updatePositions() {
        when (shape) {
            Shape.LINEAR -> {
                positions[0] = max((1f - intensity - dropoff) / 2f, 0f)
                positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = min((1f + intensity + dropoff) / 2f, 1f)
            }
            Shape.RADIAL -> {
                positions[0] = 0f
                positions[1] = min(intensity, 1f)
                positions[2] = min(intensity + dropoff, 1f)
                positions[3] = 1f
            }
            else -> {
                positions[0] = max((1f - intensity - dropoff) / 2f, 0f)
                positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = min((1f + intensity + dropoff) / 2f, 1f)
            }
        }
    }

    fun updateBounds(viewWidth: Int, viewHeight: Int) {
        val magnitude = max(viewWidth, viewHeight)
        val rad = Math.PI / 2f - Math.toRadians((tilt % 90f).toDouble())
        val hyp = magnitude / sin(rad)
        val padding = 3 * ((hyp - magnitude).toFloat() / 2f).roundToLong()
        bounds[-padding.toFloat(), -padding.toFloat(), (width(viewWidth) + padding).toFloat()] =
            (height(viewHeight) + padding).toFloat()
    }

    abstract class Builder<T : Builder<T>?> {
        val mShimmer = Shimmer()

        // Gets around unchecked cast
        protected abstract val builder: T

        /** Copies the configuration of an already built Shimmer to this builder  */
        fun copyFrom(other: Shimmer): T {
            setDirection(other.direction)
            setShape(other.shape)
            setFixedWidth(other.fixedWidth)
            setFixedHeight(other.fixedHeight)
            setWidthRatio(other.widthRatio)
            setHeightRatio(other.heightRatio)
            setIntensity(other.intensity)
            setDropoff(other.dropoff)
            setTilt(other.tilt)
            setClipToChildren(other.clipToChildren)
            setAutoStart(other.autoStart)
            setRepeatCount(other.repeatCount)
            setRepeatMode(other.repeatMode)
            setRepeatDelay(other.repeatDelay)
            setDuration(other.animationDuration)
            mShimmer.baseColor = other.baseColor
            mShimmer.highlightColor = other.highlightColor
            return builder
        }

        /** Sets the direction of the shimmer's sweep. See [Direction].  */
        fun setDirection(@Direction direction: Int): T {
            mShimmer.direction = direction
            return builder
        }

        /** Sets the shape of the shimmer. See [Shape].  */
        fun setShape(@Shape shape: Int): T {
            mShimmer.shape = shape
            return builder
        }

        /** Sets the fixed width of the shimmer, in pixels.  */
        fun setFixedWidth(@Px fixedWidth: Int): T {
            require(fixedWidth >= 0) { "Given invalid width: $fixedWidth" }
            mShimmer.fixedWidth = fixedWidth
            return builder
        }

        /** Sets the fixed height of the shimmer, in pixels.  */
        fun setFixedHeight(@Px fixedHeight: Int): T {
            require(fixedHeight >= 0) { "Given invalid height: $fixedHeight" }
            mShimmer.fixedHeight = fixedHeight
            return builder
        }

        /** Sets the width ratio of the shimmer, multiplied against the total width of the layout.  */
        fun setWidthRatio(widthRatio: Float): T {
            require(widthRatio >= 0f) { "Given invalid width ratio: $widthRatio" }
            mShimmer.widthRatio = widthRatio
            return builder
        }

        /** Sets the height ratio of the shimmer, multiplied against the total height of the layout.  */
        fun setHeightRatio(heightRatio: Float): T {
            require(heightRatio >= 0f) { "Given invalid height ratio: $heightRatio" }
            mShimmer.heightRatio = heightRatio
            return builder
        }

        /** Sets the intensity of the shimmer. A larger value causes the shimmer to be larger.  */
        fun setIntensity(intensity: Float): T {
            require(intensity >= 0f) { "Given invalid intensity value: $intensity" }
            mShimmer.intensity = intensity
            return builder
        }

        /**
         * Sets how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
         */
        fun setDropoff(dropoff: Float): T {
            require(dropoff >= 0f) { "Given invalid dropoff value: $dropoff" }
            mShimmer.dropoff = dropoff
            return builder
        }

        /** Sets the tilt angle of the shimmer in degrees.  */
        fun setTilt(tilt: Float): T {
            mShimmer.tilt = tilt
            return builder
        }

        /**
         * Sets the base alpha, which is the alpha of the underlying children, amount in the range [0,
         * 1].
         */
        fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.baseColor = intAlpha shl 24 or (mShimmer.baseColor and 0x00FFFFFF)
            return builder
        }

        /** Sets the shimmer alpha amount in the range [0, 1].  */
        fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.highlightColor = intAlpha shl 24 or (mShimmer.highlightColor and 0x00FFFFFF)
            return builder
        }

        /**
         * Sets whether the shimmer will clip to the childrens' contents, or if it will opaquely draw on
         * top of the children.
         */
        fun setClipToChildren(status: Boolean): T {
            mShimmer.clipToChildren = status
            return builder
        }

        /** Sets whether the shimmering animation will start automatically.  */
        fun setAutoStart(status: Boolean): T {
            mShimmer.autoStart = status
            return builder
        }

        /**
         * Sets how often the shimmering animation will repeat. See [ ][ValueAnimator.setRepeatCount].
         */
        fun setRepeatCount(repeatCount: Int): T {
            mShimmer.repeatCount = repeatCount
            return builder
        }

        /**
         * Sets how the shimmering animation will repeat. See [ ][ValueAnimator.setRepeatMode].
         */
        fun setRepeatMode(mode: Int): T {
            mShimmer.repeatMode = mode
            return builder
        }

        /** Sets how long to wait in between repeats of the shimmering animation.  */
        fun setRepeatDelay(millis: Long): T {
            require(millis >= 0) { "Given a negative repeat delay: $millis" }
            mShimmer.repeatDelay = millis
            return builder
        }

        /** Sets how long the shimmering animation takes to do one full sweep.  */
        fun setDuration(millis: Long): T {
            require(millis >= 0) { "Given a negative duration: $millis" }
            mShimmer.animationDuration = millis
            return builder
        }

        fun build(): Shimmer {
            mShimmer.updateColors()
            mShimmer.updatePositions()
            return mShimmer
        }

        companion object {
            private fun clamp(min: Float, max: Float, value: Float): Float {
                return kotlin.math.min(max, kotlin.math.max(min, value))
            }
        }
    }

    class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
        init {
            mShimmer.alphaShimmer = true
        }

        override val builder: AlphaHighlightBuilder
            protected get() = this
    }

    class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
        init {
            mShimmer.alphaShimmer = false
        }

        /** Sets the highlight color for the shimmer.  */
        fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.highlightColor = color
            return builder
        }

        /** Sets the base color for the shimmer.  */
        fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.baseColor = mShimmer.baseColor and -0x1000000 or (color and 0x00FFFFFF)
            return builder
        }

        override val builder: ColorHighlightBuilder
            protected get() = this
    }

    companion object {
        private const val COMPONENT_COUNT = 4
    }
}
