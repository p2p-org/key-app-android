package org.p2p.uikit.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSliderButtonBinding
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding

private const val MARGIN_HORIZONTAL_DP = 4
private const val ANIMATION_SLIDE_BACK_DURATION = 200L

class UiKitSliderButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onSlideCompleteListener: (() -> Unit)? = null

    private val binding = inflateViewBinding<WidgetSliderButtonBinding>()

    private val horizontalMargin = dip(MARGIN_HORIZONTAL_DP).toFloat()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitSliderButton).use { typedArray ->
            val icon = typedArray.getResourceId(R.styleable.UiKitSliderButton_sliderIcon, -1)
            if (icon != -1) binding.imageViewAction.setImageResource(icon)

            val text = typedArray.getText(R.styleable.UiKitSliderButton_sliderText)
            binding.textViewAction.text = text

            val textColor = typedArray.getColor(R.styleable.UiKitSliderButton_android_textColor, -1)
            binding.textViewAction.setTextColor(textColor).takeIf { textColor != -1 }
        }

        setBackgroundResource(R.drawable.bg_night_rounded_big)

        binding.containerOval.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.containerOval.clipToOutline = true

        binding.shimmerView.startShimmer()

        initializeTouchListener()
        post {
            updateGradient(horizontalMargin, horizontalMargin.toInt())
        }
    }

    override fun onDetachedFromWindow() {
        binding.shimmerView.stopShimmer()
        super.onDetachedFromWindow()
    }

    fun setupSlider(
        @StringRes actionTextRes: Int,
        @DrawableRes actionIconRes: Int,
        isLight: Boolean
    ) {
        with(binding) {
            textViewAction.setText(actionTextRes)
            imageViewAction.setImageResource(actionIconRes)
            setLightStyle(isLight)
        }
    }

    fun setLightStyle(isLight: Boolean) {
        val nightColor = getColor(R.color.night)
        val limeColor = getColor(R.color.lime)
        val whiteColor = getColor(R.color.snow)

        with(binding) {
            if (isLight) {
                setBackgroundResource(R.drawable.bg_snow_rounded_big)
                imageViewAction.setBackgroundColor(nightColor)
                imageViewAction.setColorFilter(limeColor, PorterDuff.Mode.SRC_IN)
                textViewAction.setTextColor(nightColor)
            } else {
                setBackgroundResource(R.drawable.bg_night_rounded_big)
                imageViewAction.setBackgroundColor(limeColor)
                imageViewAction.setColorFilter(nightColor, PorterDuff.Mode.SRC_IN)
                textViewAction.setTextColor(whiteColor)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeTouchListener() {
        val initialPosition = horizontalMargin

        with(binding) {
            shimmerView.setOnTouchListener { view, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> onActionDown()
                    MotionEvent.ACTION_MOVE -> onActionMove(view, initialPosition, event)
                    MotionEvent.ACTION_UP -> onActionUp(view, initialPosition)
                    // we assume we are handling all events,
                    // otherwise above events are not working correctly
                    else -> true
                }
            }
        }
    }

    private fun onActionUp(view: View, initialPosition: Float): Boolean {
        val currentPosition = view.x
        val lastPosition = measuredWidth - view.width - initialPosition

        // if user didn't slide fully animating back
        if (currentPosition < lastPosition) {
            ValueAnimator.ofFloat(view.x, initialPosition).apply {
                duration = ANIMATION_SLIDE_BACK_DURATION
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val animationPosition = it.animatedValue.toString().toFloat()
                    view.x = animationPosition
                    updateGradient(initialPosition, animationPosition.toInt() + view.width / 2)
                    if (animationPosition == initialPosition) {
                        setGradientVisible(isVisible = false)
                    }
                }
                start()
            }
        } else {
            // user slided till the end
            onSlideCompleteListener?.invoke()
        }
        return true
    }

    private fun onActionMove(
        view: View,
        initialPosition: Float,
        event: MotionEvent
    ): Boolean {
        val lastPosition = measuredWidth - view.width - initialPosition
        val imagePosition = (event.rawX - view.width).coerceIn(initialPosition, lastPosition)
        view.x = imagePosition

        val gradientPosition = event.rawX.coerceIn(initialPosition, measuredWidth - horizontalMargin - initialPosition)
        updateGradient(initialPosition, gradientPosition.toInt())
        setGradientVisible(isVisible = true)
        return true
    }

    private fun onActionDown(): Boolean {
        setGradientVisible(isVisible = true)
        return true
    }

    private fun updateGradient(startPosition: Float, newPositionX: Int) {
        with(binding) {
            val oldParams = viewGradient.layoutParams
            oldParams.width = newPositionX.coerceAtLeast(imageViewAction.width)
            viewGradient.layoutParams = oldParams
            viewGradient.x = startPosition
        }
    }

    private fun setGradientVisible(isVisible: Boolean) {
        binding.viewGradient.isVisible = isVisible
    }
}
