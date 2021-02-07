package com.p2p.wowlet.fragment.dashboard.dialog.swap.utils

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.dashboard.dialog.swap.adapter.SelectTokenToSwapAdapter
import com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.supportclass.widget.CheckableButtonAttrs
import com.p2p.wowlet.supportclass.widget.CheckableButtonGroup

@BindingAdapter(
    "buttonStateChecked",
    "textColorChecked",
    "buttonStateUnchecked",
    "textColorUnchecked"
)
fun CheckableButtonGroup.setButtonsCheckedUncheckedStates(
    @DrawableRes drawableChecked: Int,
    @ColorRes textChecked: Int,
    @DrawableRes drawableUnchecked: Int,
    @ColorRes textUnchecked: Int
) {

    setButtonStates(drawableChecked, textChecked, drawableUnchecked, textUnchecked)
}

@BindingAdapter(
    "buttonStateChecked",
    "textColorChecked",
    "buttonStateUnchecked",
    "textColorUnchecked",
    "attrs"
)
fun CheckableButtonGroup.setButtonsCheckedUncheckedStates(
    @DrawableRes drawableChecked: Int,
    @ColorRes textChecked: Int,
    @DrawableRes drawableUnchecked: Int,
    @ColorRes textUnchecked: Int,
    specialAttrs: Map<Int, CheckableButtonAttrs>?
) {
    setAttributesForSpecialPositions(specialAttrs)
    setButtonStates(drawableChecked, textChecked, drawableUnchecked, textUnchecked)
}



@BindingAdapter("enableDecimalInputTextWatcher")
fun AppCompatEditText.enableDecimalInputTextWatcher(enable: Boolean) {
    if (!enable) return
    doAfterTextChanged {
        if (it.toString() == ".") {
            setText("0.")
            setSelection(it.toString().length + 1)
        }
    }
}

@BindingAdapter("postTintOnFocusChange")
fun AppCompatEditText.postTintOnFocusChange(viewModel: SwapViewModel) {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            viewModel.setTintOnSearchBarFocusChange(R.color.black)
            viewModel.setCloseIconVisibility(true)
        }else {
            viewModel.setTintOnSearchBarFocusChange(R.color.gray_blue_400)
            viewModel.setCloseIconVisibility(false)
        }
    }
}

@BindingAdapter("tint")
fun AppCompatImageView.setTint(tint: Int) {
    setColorFilter(ContextCompat.getColor(context, tint))
}





















