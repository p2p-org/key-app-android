package org.p2p.wallet.common.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.roundToInt

private const val KEY_SUPER_STATE = "super_state"
private const val KEY_EXPANSION = "expansion"
private const val DEFAULT_DURATION = 250

class ExpandLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var duration: Int = DEFAULT_DURATION
    private var parallax = 0f
    private var expansion = 0f

    private var state: State = State.EXPANDED
    private var interpolator: Interpolator = FastOutSlowInInterpolator()
    private var animator: ValueAnimator? = null
    var onExpansionChangeListener: ((expansion: Float, state: State) -> Unit)? = null

    enum class State {
        COLLAPSED,
        COLLAPSING,
        EXPANDING,
        EXPANDED;
    }

    init {
        if (attrs != null) {
            duration = 250
            orientation = VERTICAL
            parallax = 1f
            setParallax(parallax)
        }
    }

    fun getState() = state

    fun setup(
        isExpanded: Boolean = true
    ) {
        this.state = if (isExpanded) State.EXPANDED else State.COLLAPSED
        this.expansion = if (state == State.EXPANDED) 1f else 0f
        onExpansionChangeListener?.invoke(expansion, state)
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        expansion = if (isExpanded) 1f else 0.toFloat()
        bundle.putFloat(
            KEY_EXPANSION,
            expansion
        )
        bundle.putParcelable(
            KEY_SUPER_STATE,
            superState
        )
        return bundle
    }

    override fun onRestoreInstanceState(parcelable: Parcelable) {
        val bundle = (parcelable as Bundle).also {
            expansion =
                it.getFloat(KEY_EXPANSION)
        }
        state = if (expansion == 1f) State.EXPANDED else State.COLLAPSED
        onExpansionChangeListener?.invoke(expansion, state)
        val superState =
            bundle.getParcelable<Parcelable>(KEY_SUPER_STATE)
        super.onRestoreInstanceState(superState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        visibility = if (expansion == 0f && height == 0) View.GONE else View.VISIBLE
        val expansionDelta = height - (height * expansion).roundToInt()
        if (parallax > 0) {
            val parallaxDelta = expansionDelta * parallax
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.translationY = -parallaxDelta
            }
        }
        setMeasuredDimension(width, height - expansionDelta)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        animator?.cancel()
        super.onConfigurationChanged(newConfig)
    }

    var isExpanded: Boolean
        get() = state == State.EXPANDING || state == State.EXPANDED
        private set(expand) {
            setExpanded(expand, true)
        }

    @JvmOverloads
    fun toggle(animate: Boolean = true) {
        if (isExpanded) {
            collapse(animate)
        } else {
            expand(animate)
        }
    }

    @JvmOverloads
    fun expand(animate: Boolean = true) {
        setExpanded(true, animate)
    }

    @JvmOverloads
    fun collapse(animate: Boolean = true) {
        setExpanded(false, animate)
    }

    private fun setExpanded(expand: Boolean, animate: Boolean) {
        if (expand == isExpanded) {
            return
        }
        val targetExpansion = if (expand) 1 else 0
        if (animate) {
            animateSize(targetExpansion)
        } else {
            setExpansion(targetExpansion.toFloat())
        }
    }

    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    fun getExpansion(): Float {
        return expansion
    }

    fun setExpansion(expansion: Float) {
        if (this.expansion == expansion) {
            return
        }
        updateState(expansion)
        visibility = if (state == State.COLLAPSED) View.GONE else View.VISIBLE
        this.expansion = expansion
        requestLayout()
        onExpansionChangeListener?.invoke(this.expansion, state)
    }

    private fun updateState(expansion: Float) {
        // Infer state from previous value
        val delta = expansion - this.expansion
        when {
            expansion == 0f -> {
                state = State.COLLAPSED
            }
            expansion == 1f -> {
                state = State.EXPANDED
            }
            delta < 0 -> {
                state = State.COLLAPSING
            }
            delta > 0 -> {
                state = State.EXPANDING
            }
        }
    }

    fun getParallax(): Float {
        return parallax
    }

    fun setParallax(parallax: Float) {
        // Make sure parallax is between 0 and 1
        var parallax = parallax
        parallax = Math.min(1f, Math.max(0f, parallax))
        this.parallax = parallax
    }

    private fun animateSize(targetExpansion: Int) {
        if (animator != null) {
            animator!!.cancel()
            animator = null
        }
        animator = ValueAnimator.ofFloat(expansion, targetExpansion.toFloat())
        animator?.interpolator = interpolator
        animator?.duration = duration.toLong()
        animator?.addUpdateListener { valueAnimator ->
            setExpansion(
                valueAnimator.animatedValue as Float
            )
        }
        animator?.addListener(
            ExpansionListener(
                targetExpansion
            )
        )
        animator?.start()
    }

    private inner class ExpansionListener(private val targetExpansion: Int) :
        Animator.AnimatorListener {
        private var canceled = false
        override fun onAnimationStart(animation: Animator) {
            state = if (targetExpansion == 0) State.COLLAPSING else State.EXPANDING
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!canceled) {
                state = if (targetExpansion == 0) State.COLLAPSED else State.EXPANDED
                setExpansion(targetExpansion.toFloat())
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }

        override fun onAnimationRepeat(animation: Animator) {}
    }
}