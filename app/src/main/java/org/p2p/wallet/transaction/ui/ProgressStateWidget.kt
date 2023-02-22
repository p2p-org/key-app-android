package org.p2p.wallet.transaction.ui

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import java.util.concurrent.TimeUnit
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetProgressStateBinding

private const val STATUS_ANIMATION_DURATION = 200L

class ProgressStateWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetProgressStateBinding = inflateViewBinding()

    init {
        RotateAnimation(
            0F, 360F,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).let { rotate ->
            rotate.duration = TimeUnit.SECONDS.toMillis(1)
            rotate.repeatCount = Animation.INFINITE
            binding.progressIndicator.startAnimation(rotate)
        }
    }

    fun setDescriptionText(@StringRes textRes: Int) {
        binding.textViewDescription.setText(textRes)
    }

    fun setDescriptionText(text: String?) {
        binding.textViewDescription.text = text
    }

    fun setSuccessState() = with(binding) {
        progressIndicator.clearAnimation()
        progressIndicator.isVisible = false
        imageViewProgressIcon.setImageResource(R.drawable.ic_lightning)
        startChangeIconColorAnimation(R.color.icons_night, R.color.icons_grass) {
            startChangeBackGroundColorAnimation(binding.imageViewProgressIcon, R.color.bg_rain, R.color.light_grass)
            startChangeBackGroundColorAnimation(binding.containerWidget, R.color.bg_cloud, R.color.light_grass_30)
        }
    }

    fun setFailedState() = with(binding) {
        progressIndicator.clearAnimation()
        progressIndicator.isVisible = false
        imageViewProgressIcon.setImageResource(R.drawable.ic_warning_solid)
        startChangeIconColorAnimation(R.color.icons_night, R.color.icons_rose) {
            startChangeBackGroundColorAnimation(binding.imageViewProgressIcon, R.color.bg_rain, R.color.light_rose)
            startChangeBackGroundColorAnimation(binding.containerWidget, R.color.bg_cloud, R.color.light_rose_30)
        }
    }

    private fun startChangeIconColorAnimation(
        @ColorRes fromColorRes: Int,
        @ColorRes toColorRes: Int,
        doAfterBlock: () -> Unit = {}
    ) {
        startChangeColorAnimation(fromColorRes, toColorRes, doAfterBlock) { value ->
            binding.imageViewProgressIcon.setColorFilter(
                value,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun startChangeBackGroundColorAnimation(
        view: View,
        @ColorRes fromColorRes: Int,
        @ColorRes toColorRes: Int,
        doAfterBlock: () -> Unit = {}
    ) {
        startChangeColorAnimation(fromColorRes, toColorRes, doAfterBlock) { value ->
            val colorFilter = PorterDuffColorFilter(
                value,
                PorterDuff.Mode.MULTIPLY
            )
            view.background.colorFilter = colorFilter
        }
    }

    private fun startChangeColorAnimation(
        @ColorRes fromColorRes: Int,
        @ColorRes toColorRes: Int,
        doAfterBlock: () -> Unit,
        animatorDelegate: (value: Int) -> Unit
    ) {
        val colorFrom: Int = binding.getColor(fromColorRes)
        val colorTo: Int = binding.getColor(toColorRes)
        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = STATUS_ANIMATION_DURATION
            addUpdateListener { animator ->
                animatorDelegate(animator.animatedValue as Int)
            }
            addListener(onEnd = { doAfterBlock.invoke() })
            start()
        }
    }
}
