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
import androidx.core.animation.addListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.p2p.wallet.utils.getParcelableCompat

private const val KEY_SUPER_STATE = "super_state"
private const val KEY_EXPANSION = "expansion"
private const val DEFAULT_DURATION = 250
private const val MAX_EXPANSION = 1f
private const val MIN_EXPANSION = 0f

class ExpandLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
    }
    private var parallax: Float = 1f
        set(value) {
            val previous = min(1f, max(0f, value))
            field = previous
        }

    private var expansion = 0f
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateState(value)
            visibility = if (state == State.COLLAPSED) View.GONE else View.VISIBLE
            requestLayout()
            onExpansionChangeListener?.invoke(this.expansion, state)
        }

    var isExpanded: Boolean
        get() = state == State.EXPANDING || state == State.EXPANDED
        private set(expand) {
            setExpanded(expand, true)
        }

    var state: State = State.EXPANDED
    private var interpolator: Interpolator = FastOutSlowInInterpolator()
    private var animator: ValueAnimator? = null
    var onExpansionChangeListener: ((expansion: Float, state: State) -> Unit)? = null

    enum class State {
        COLLAPSED,
        COLLAPSING,
        EXPANDING,
        EXPANDED;
    }

    fun setup(
        isExpanded: Boolean = true
    ) {
        this.state = if (isExpanded) State.EXPANDED else State.COLLAPSED
        this.expansion = if (state == State.EXPANDED) MAX_EXPANSION else MIN_EXPANSION
        onExpansionChangeListener?.invoke(expansion, state)
        requestLayout()
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
            expansion = targetExpansion.toFloat()
        }
    }

    private fun updateState(expansion: Float) {
        // Infer state from previous value
        val delta = expansion - this.expansion
        when {
            expansion == MIN_EXPANSION -> {
                state = State.COLLAPSED
            }
            expansion == MAX_EXPANSION -> {
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

    private fun animateSize(targetExpansion: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(expansion, targetExpansion.toFloat())?.apply {
            interpolator = this@ExpandLinearLayout.interpolator
            duration = DEFAULT_DURATION.toLong()
            addUpdateListener { valueAnimator ->
                expansion = valueAnimator.animatedValue as Float
            }
            addListener(ExpansionListener(targetExpansion))
        }
        animator?.start()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        expansion = if (isExpanded) MAX_EXPANSION else MIN_EXPANSION
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
            bundle.getParcelableCompat<Parcelable>(KEY_SUPER_STATE)
        super.onRestoreInstanceState(superState)
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
                expansion = targetExpansion.toFloat()
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }

        override fun onAnimationRepeat(animation: Animator) {}
    }
}
