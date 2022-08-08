package org.p2p.wallet.auth.widget

import android.animation.Animator
import android.content.Context
import android.widget.RelativeLayout
import android.view.ViewAnimationUtils
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.core.view.isVisible
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.hideKeyboard
import org.p2p.wallet.databinding.WidgetSearchViewBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class AnimatedSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetSearchViewBinding>()
    private var animator: Animator? = null
    private var stateListener: SearchStateListener? = null

    init {
        binding.showButtonContainer.setOnClickListener { openSearch() }
        binding.closeButton.setOnClickListener { closeSearch() }
    }

    private fun openSearch() = with(binding) {
        editText.setText("")
        searchContainer.visibility = VISIBLE
        if (animator != null) animator!!.cancel()
        animator = ViewAnimationUtils.createCircularReveal(
            searchContainer,
            searchContainer.width + (showButton.right + showButton.left) / 2,
            (showButton.top + showButton.bottom) / 2,
            0f,
            width
                .toFloat()
        )
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                editText.focusAndShowKeyboard()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        animator?.duration = 200
        animator?.start()
    }

    fun closeSearch() = with(binding) {
        if (animator != null) animator!!.cancel()
        editText.hideKeyboard()
        animator = ViewAnimationUtils.createCircularReveal(
            searchContainer,
            searchContainer.width + (showButton.right + showButton.left) / 2,
            (showButton.top + showButton.bottom) / 2,
            width.toFloat(), 0f
        )
        animator?.duration = 200
        animator?.start()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                searchContainer!!.visibility = INVISIBLE
                editText!!.setText("")
                animator.removeAllListeners()
                if (stateListener != null) stateListener!!.onClosed()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    fun setStateListener(listener: SearchStateListener?) {
        stateListener = listener
    }

    fun addTextWatcher(textWatcher: TextWatcher?) {
        binding.editText.addTextChangedListener(textWatcher)
    }

    fun removeTextWatcher(textWatcher: TextWatcher?) {
        binding.editText.removeTextChangedListener(textWatcher)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator != null) animator!!.cancel()
    }

    fun isBackPressEnabled(): Boolean = binding.searchContainer.isVisible

    interface SearchStateListener {
        fun onClosed()
    }
}
