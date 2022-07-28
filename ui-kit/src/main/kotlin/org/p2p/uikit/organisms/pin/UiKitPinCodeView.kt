package org.p2p.uikit.organisms.pin

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.p2p.uikit.R

private const val ANIMATION_DURATION = 400L
private const val DOT_DELTA = 12
private const val X_DELTA = 4f
private const val Y_DELTA = 0f

class UiKitPinCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var roundViews = mutableListOf<ImageView>()
    private var emptyDotDrawableId: Drawable? = null
    private var fullDotDrawableId: Drawable? = null

    var currentLength: Int = 0
        private set

    init {
        emptyDotDrawableId = ContextCompat.getDrawable(context, R.drawable.ic_new_dot_empty)
        fullDotDrawableId = ContextCompat.getDrawable(context, R.drawable.ic_new_dot_full)
        gravity = Gravity.CENTER
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
    }

    fun startErrorAnimation(onAnimationFinished: () -> Unit) {
        TranslateAnimation(-X_DELTA, X_DELTA, Y_DELTA, Y_DELTA).apply {
            duration = ANIMATION_DURATION
            repeatMode = Animation.REVERSE
            repeatCount = 2
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    setDotsColor(null)
                    onAnimationFinished()
                }

                override fun onAnimationRepeat(animation: Animation?) = Unit

                override fun onAnimationStart(animation: Animation?) {
                    setDotsColor(R.color.rose)
                }
            })

            startAnimation(this)
        }
    }

    fun startSuccessAnimation(onAnimationFinished: () -> Unit) {
        TranslateAnimation(0f, 0f, 0f, 0f).apply {
            duration = 1000L
            repeatMode = Animation.INFINITE
            repeatCount = 2
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    setDotsColor(R.color.mint)
                }

                override fun onAnimationEnd(animation: Animation?) {
                    setDotsColor(null)
                    onAnimationFinished()
                }

                override fun onAnimationRepeat(animation: Animation?) = Unit
            })
            startAnimation(this)
        }
    }

    fun setPinLength(pinLength: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        removeAllViews()
        val temp = mutableListOf<ImageView>()
        for (i in 0 until pinLength) {
            val roundView = if (i < roundViews.size) {
                roundViews[i]
            } else {
                inflater.inflate(R.layout.widget_ui_kit_pin_code_view_dot, this, false) as ImageView
            }
            addView(roundView)
            temp.add(roundView)
        }
        roundViews.clear()
        roundViews.addAll(temp)

        refresh(0)
    }

    private fun setDotsColor(@ColorRes resourceId: Int?) {
        roundViews.forEach {
            if (resourceId == null) {
                it.clearColorFilter()
            } else {
                it.setColorFilter(context.getColor(resourceId))
            }
        }
    }
}
