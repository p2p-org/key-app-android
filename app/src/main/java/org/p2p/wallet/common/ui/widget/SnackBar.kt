package org.p2p.wallet.common.ui.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.snackbar.ContentViewCallback
import org.p2p.wallet.databinding.WidgetSnackbarBinding
import org.p2p.wallet.utils.getString

private const val ANIMATION_DURATION = 500L

class SnackBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    private val binding = WidgetSnackbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        clipToPadding = false
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        val scaleX = ObjectAnimator.ofFloat(binding.actionImageView, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.actionImageView, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            setDuration(ANIMATION_DURATION)
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }

    fun setMessage(text: String): SnackBar {
        binding.textView.text = text
        return this
    }

    fun setIcon(@DrawableRes iconResId: Int): SnackBar {
        binding.imageView.setImageResource(iconResId)
        binding.imageView.isVisible = true
        return this
    }

    fun setAction(actionResId: Int?, block: (() -> Unit)?) {
        if (actionResId != null && block != null) {
            binding.actionTextView.text = getString(actionResId)
            binding.actionTextView.setOnClickListener { block.invoke() }
            binding.actionTextView.isVisible = true
        }
    }
}
