package org.p2p.uikit.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSliderSolidButtonBinding
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding

private const val MARGIN_HORIZONTAL_DP = 4
private const val COMPLETED_VIEW_WIDTH_DP = 56
private const val ANIMATION_SLIDE_BACK_DURATION = 200L

private const val START_TEXT_ALPHA = 0f
private const val END_TEXT_ALPHA = 1f

class UiKitSliderSolidButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onSlideCompleteListener: (() -> Unit)? = null
    var onSlideCollapseCompleted: (() -> Unit)? = null

    private val binding = inflateViewBinding<WidgetSliderSolidButtonBinding>()

    private val horizontalMargin = dip(MARGIN_HORIZONTAL_DP).toFloat()
    private val completedWidth = dip(COMPLETED_VIEW_WIDTH_DP)

    private val depositButtonsAnimation = ChangeBounds().apply {
        duration = ANIMATION_SLIDE_BACK_DURATION
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitSliderSolidButton).use { typedArray ->
            val icon = typedArray.getResourceId(R.styleable.UiKitSliderSolidButton_sliderIcon, -1)
            if (icon != -1) binding.imageViewAction.setImageResource(icon)

            val text = typedArray.getText(R.styleable.UiKitSliderSolidButton_sliderText)
            binding.textViewAction.text = text
            binding.textViewActionTop.text = text
        }

        setBackgroundResource(R.drawable.bg_night_rounded_big)

        binding.containerOval.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.containerOval.clipToOutline = true

        initializeTouchListener()
        post {
            updateGradient(horizontalMargin, horizontalMargin.toInt())
        }
    }

    fun setupSlider(
        @StringRes actionTextRes: Int,
        @DrawableRes actionIconRes: Int,
        isLight: Boolean
    ) {
        with(binding) {
            textViewAction.setText(actionTextRes)
            textViewActionTop.setText(actionTextRes)
            imageViewAction.setImageResource(actionIconRes)
            setLightStyle(isLight)
        }
    }

    fun setActionText(@StringRes actionTextRes: Int) = with(binding) {
        textViewAction.setText(actionTextRes)
        textViewActionTop.setText(actionTextRes)
    }

    fun setActionText(actionText: String) = with(binding) {
        textViewAction.text = actionText
        textViewActionTop.text = actionText
    }

    fun setActionText(actionText: TextContainer) = with(binding) {
        actionText.applyTo(textViewAction)
        actionText.applyTo(textViewActionTop)
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
    fun showCompleteAnimation() = with(binding) {
        shimmerView.setOnTouchListener { _, _ -> true }
        setGradientVisible(isVisible = false)
        textViewAction.isVisible = false
        textViewActionTop.isVisible = false
        imageViewAction.setImageDrawable(null)
        TransitionManager.beginDelayedTransition(
            root.parent as ViewGroup,
            depositButtonsAnimation
        )
        val params = root.layoutParams
        params.width = completedWidth
        root.layoutParams = params
        shimmerView.x = horizontalMargin
        imageViewAction.postDelayed(
            { animateTick() },
            ANIMATION_SLIDE_BACK_DURATION
        )
    }

    fun restoreSlider() {
        updateGradient(horizontalMargin, horizontalMargin.toInt())
        binding.shimmerView.x = horizontalMargin
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
                        updateTextsAlpha(START_TEXT_ALPHA)
                    }
                }
                start()
            }
        } else {
            updateTextsAlpha(END_TEXT_ALPHA)
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
        updateTextsAlpha(startPosition, newPositionX)
    }

    private fun updateTextsAlpha(startPosition: Float, currentPosition: Int) = with(binding) {
        val progress = (startPosition / 100 * currentPosition)
        val nextAlpha = progress / 100
        updateTextsAlpha(nextAlpha)
    }

    private fun updateTextsAlpha(nextAlpha: Float) = with(binding) {
        textViewAction.alpha = 1f - nextAlpha
        textViewActionTop.alpha = nextAlpha
    }

    private fun setGradientVisible(isVisible: Boolean) {
        binding.viewGradient.isVisible = isVisible
    }

    private fun animateTick() = with(binding) {
        imageViewAction.setImageResource(R.drawable.ic_check_animated)
        (imageViewAction.drawable as? Animatable)?.start()
        imageViewAction.postDelayed(
            { onSlideCollapseCompleted?.invoke() },
            ANIMATION_SLIDE_BACK_DURATION
        )
    }
}
