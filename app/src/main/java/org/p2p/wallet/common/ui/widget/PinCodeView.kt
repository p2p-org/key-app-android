package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetPinCodeViewBinding
import org.p2p.wallet.utils.dip

private const val ANIMATION_DURATION = 400L
private const val DOT_STROKE_WIDTH = 24
private const val DOT_DELTA = 12

class PinCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var roundViews = mutableListOf<ImageView>()
    private var emptyDotDrawableId: Drawable? = null
    private var fullDotDrawableId: Drawable? = null

    var currentLength: Int = 0
        private set

    private val binding = WidgetPinCodeViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        attrs.let {
            emptyDotDrawableId = ContextCompat.getDrawable(context, R.drawable.ic_dot_empty)
            fullDotDrawableId = ContextCompat.getDrawable(context, R.drawable.ic_dot_full)
        }
    }

    fun refresh(pinLength: Int) {
        currentLength = pinLength
        for (i in roundViews.indices) {
            if (i <= pinLength - 1) {
                roundViews[i].setImageDrawable(fullDotDrawableId)
            } else {
                roundViews[i].setImageDrawable(emptyDotDrawableId)
            }
        }
        showProgress(pinLength)
    }

    private fun showProgress(length: Int) {
        val width = when (length) {
            0 -> 0
            1 -> DOT_STROKE_WIDTH
            else -> {
                DOT_STROKE_WIDTH + (DOT_STROKE_WIDTH + DOT_DELTA) * (length - 1)
            }
        }.toInt()
        val lp = binding.progressView.layoutParams as LayoutParams
        lp.width = dip(width)
        binding.progressView.layoutParams = lp
    }

    @SuppressWarnings("MagicNumber")
    fun startErrorAnimation(onAnimationFinished: () -> Unit) {
        val animation = TranslateAnimation(-4f, 4f, 0f, 0f)
        animation.duration = ANIMATION_DURATION
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = 2
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                setDotsColor(null, null)
                onAnimationFinished()
            }

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationStart(animation: Animation?) {
                setDotsColor(R.color.systemErrorMain, R.color.systemErrorSecondary)
            }
        })
        startAnimation(animation)
    }

    fun startSuccessAnimation(onAnimationFinished: () -> Unit) {
        val animation = TranslateAnimation(0f, 0f, 0f, 0f)
        animation.duration = 1000L
        animation.repeatMode = Animation.INFINITE
        animation.repeatCount = 2
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                setDotsColor(
                    R.color.systemSuccess,
                    R.color.systemSuccessSecondary
                )
            }

            override fun onAnimationEnd(animation: Animation?) {
                setDotsColor(null, null)
                onAnimationFinished()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        startAnimation(animation)
    }

    fun setPinLength(pinLength: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.container.removeAllViews()
        val temp = mutableListOf<ImageView>()
        for (i in 0 until pinLength) {
            val roundView = if (i < roundViews.size) {
                roundViews[i]
            } else {
                inflater.inflate(R.layout.view_pin_code_dot, binding.container, false) as ImageView
            }
            binding.container.addView(roundView)
            temp.add(roundView)
        }
        roundViews.clear()
        roundViews.addAll(temp)

        refresh(0)
    }

    private fun setDotsColor(@ColorRes resourceId: Int?, @ColorRes backgroundColor: Int?) {
        roundViews.forEach {
            if (resourceId == null) {
                it.clearColorFilter()
            } else {
                it.setColorFilter(context.getColor(resourceId))
            }
        }
        val bg = binding.progressView.background.mutate()
        bg.setTint(context.getColor(backgroundColor ?: R.color.textIconLink))
        binding.progressView.background = bg
    }
}
