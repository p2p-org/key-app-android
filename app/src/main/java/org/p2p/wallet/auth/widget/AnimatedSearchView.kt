package org.p2p.wallet.auth.widget

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import android.animation.Animator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.widget.RelativeLayout
import org.p2p.core.utils.hideKeyboard
import org.p2p.core.utils.showKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.databinding.WidgetSearchViewBinding
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class AnimatedSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetSearchViewBinding>()
    private var animator: Animator? = null
    private var stateListener: SearchStateListener? = null

    init {
        binding.apply {
            relativeLayoutContainer.setOnClickListener { openSearch() }
            imageViewErase.setOnClickListener { editTextSearch.text.clear() }
            buttonClose.setOnClickListener { closeSearch() }
            editTextSearch.doOnTextChanged { text, _, _, _ ->
                imageViewErase.isVisible = !text.isNullOrEmpty()
            }
        }
    }

    fun openSearch() = with(binding) {
        editTextSearch.setText("")
        relativeLayoutSearchContainer.visibility = VISIBLE
        if (animator != null) animator!!.cancel()
        animator = ViewAnimationUtils.createCircularReveal(
            relativeLayoutSearchContainer,
            relativeLayoutSearchContainer.width + (buttonShowSearch.right + buttonShowSearch.left) / 2,
            (buttonShowSearch.top + buttonShowSearch.bottom) / 2,
            0f,
            width
                .toFloat()
        )
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) = Unit
            override fun onAnimationEnd(animator: Animator) {
                editTextSearch.requestFocus()
                editTextSearch.showKeyboard()
            }

            override fun onAnimationCancel(animator: Animator) = Unit
            override fun onAnimationRepeat(animator: Animator) = Unit
        })
        animator?.duration = 200
        animator?.start()
    }

    fun closeSearch() = with(binding) {
        if (animator != null) animator!!.cancel()
        editTextSearch.hideKeyboard()
        animator = ViewAnimationUtils.createCircularReveal(
            buttonShowSearch,
            relativeLayoutSearchContainer.width + (buttonShowSearch.right + buttonShowSearch.left) / 2,
            (buttonShowSearch.top + buttonShowSearch.bottom) / 2,
            width.toFloat(), 0f
        )
        animator?.duration = 200
        animator?.start()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) = Unit
            override fun onAnimationEnd(animator: Animator) {
                relativeLayoutSearchContainer.visibility = INVISIBLE
                editTextSearch.setText("")
                animator.removeAllListeners()
                if (stateListener != null) stateListener!!.onClosed()
            }

            override fun onAnimationCancel(animator: Animator) = Unit
            override fun onAnimationRepeat(animator: Animator) = Unit
        })
    }

    fun setStateListener(listener: SearchStateListener?) {
        stateListener = listener
    }

    fun addTextWatcher(textWatcher: TextWatcher?) {
        binding.editTextSearch.addTextChangedListener(textWatcher)
    }

    fun doAfterTextChanged(block: (Editable?) -> Unit) {
        binding.editTextSearch.doAfterTextChanged(block)
    }

    fun removeTextWatcher(textWatcher: TextWatcher?) {
        binding.editTextSearch.removeTextChangedListener(textWatcher)
    }

    fun setBgColor(@ColorRes colorRes: Int) {
        binding.relativeLayoutContainer.setBackgroundColor(getColor(colorRes))
        binding.relativeLayoutSearchContainer.setBackgroundColor(getColor(colorRes))
    }

    fun setHint(@StringRes titleResId: Int) {
        binding.editTextSearch.hint = binding.getString(titleResId)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator != null) animator!!.cancel()
    }

    fun isBackPressEnabled(): Boolean = binding.relativeLayoutSearchContainer.isVisible

    fun interface SearchStateListener {
        fun onClosed()
    }
}
