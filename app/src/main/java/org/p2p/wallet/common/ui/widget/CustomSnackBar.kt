package org.p2p.wallet.common.ui.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.snackbar.ContentViewCallback
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.WidgetSnackbarBinding
import org.p2p.wallet.utils.NoOp
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val ANIMATION_DURATION = 500L

class CustomSnackBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    private val binding = inflateViewBinding<WidgetSnackbarBinding>()

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

    override fun animateContentOut(delay: Int, duration: Int) = NoOp

    fun setMessage(text: String): CustomSnackBar {
        binding.textView.text = text
        return this
    }

    fun setIcon(@DrawableRes iconResId: Int): CustomSnackBar {
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
