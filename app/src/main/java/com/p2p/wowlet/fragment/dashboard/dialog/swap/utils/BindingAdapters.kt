package com.p2p.wowlet.fragment.dashboard.dialog.swap.utils

import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel

fun AppCompatEditText.enableDecimalInputTextWatcher(enable: Boolean) {
    if (!enable) return
    doAfterTextChanged {
        if (it.toString() == ".") {
            setText("0.")
            setSelection(it.toString().length + 1)
        }
    }
}

fun AppCompatEditText.postTintOnFocusChange(viewModel: SwapViewModel) {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            viewModel.setTintOnSearchBarFocusChange(R.color.black)
            viewModel.setCloseIconVisibility(true)
        } else {
            viewModel.setTintOnSearchBarFocusChange(R.color.gray_blue_400)
            viewModel.setCloseIconVisibility(false)
        }
    }
}

fun AppCompatImageView.setTint(tint: Int) {
    setColorFilter(ContextCompat.getColor(context, tint))
}





















