package org.p2p.uikit.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewOutlineProvider
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

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitSliderButton).use { typedArray ->
            val icon = typedArray.getResourceId(R.styleable.UiKitSliderButton_sliderIcon, -1)
            if (icon != -1) binding.imageViewAction.setImageResource(icon)

            val text = typedArray.getText(R.styleable.UiKitSliderButton_sliderText)
            binding.textViewAction.text = text
        }

        setBackgroundResource(R.drawable.bg_night_rounded_big)

        binding.containerOval.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.containerOval.clipToOutline = true

        binding.shimmerView.startShimmer()

        initializeTouchListener()
    }

    override fun onDetachedFromWindow() {
        binding.shimmerView.stopShimmer()
        super.onDetachedFromWindow()
    }

    fun setupView(
        @StringRes textRes: Int,
        @DrawableRes iconRes: Int,
        isLight: Boolean
    ) {
        with(binding) {
            imageViewAction
            textViewAction.setText(textRes)
            imageViewAction.setImageResource(iconRes)
            setLight(isLight)
        }
    }

    fun setLight(isLight: Boolean) {
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
        val initialPosition = dip(MARGIN_HORIZONTAL_DP).toFloat()

        with(binding) {
            containerOval.setOnTouchListener { view, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        setGradientVisible(isVisible = true)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val lastPosition = measuredWidth - view.width - initialPosition
                        val imagePosition = (event.rawX - view.width).coerceIn(initialPosition, lastPosition)
                        view.x = imagePosition

                        val gradientPosition = event.rawX.coerceIn(initialPosition, measuredWidth - initialPosition)
                        updateGradient(initialPosition, gradientPosition.toInt())
                        setGradientVisible(isVisible = true)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val currentPosition = view.x
                        val lastPosition = measuredWidth - view.width - initialPosition

                        // if user didn't slide fully animating back
                        if (currentPosition < lastPosition) {
                            view.animate()
                                .x(initialPosition)
                                .setDuration(ANIMATION_SLIDE_BACK_DURATION)
                                .start()

                            updateGradient(initialPosition, initialPosition.toInt())
                            setGradientVisible(isVisible = false)
                        } else {
                            // user slided till the end
                            onSlideCompleteListener?.invoke()
                        }
                        true
                    }
                    // we assume we are handling all events,
                    // otherwise above events are not working correctly
                    else ->
                        true
                }
            }
        }
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
