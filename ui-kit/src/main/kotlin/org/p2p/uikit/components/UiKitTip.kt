package org.p2p.uikit.components

import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetTipViewBinding
import org.p2p.uikit.utils.toPx

enum class TipPosition {
    TOP,
    BOTTOM
}

fun showTip(
    anchorView: View,
    counterText: String,
    @StringRes title: Int,
    @StringRes buttonNextTitle: Int = R.string.tip_button_next_title,
    @StringRes buttonSkipTitle: Int = R.string.tip_button_skip_all_title,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val context = anchorView.context
    showTip(
        anchorView,
        counterText,
        context.getString(title),
        context.getString(buttonNextTitle),
        context.getString(buttonSkipTitle),
        onNextClick,
        onSkipClick
    )
}

fun showTip(
    anchorView: View,
    counterText: String,
    title: String,
    buttonNextTitle: String,
    buttonSkipTitle: String,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val binding = WidgetTipViewBinding.inflate(LayoutInflater.from(anchorView.context)).apply {
        countTextView.text = counterText
        titleTextView.text = title
        nextButton.text = buttonNextTitle
        skipAllButton.text = buttonSkipTitle
        nextButton.setOnClickListener { onNextClick() }
        skipAllButton.setOnClickListener { onSkipClick() }
    }.apply {
        root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    }
    val popupView = binding.root

    val tipGravity = getTipPosition(popupView, anchorView)
    setupArrow(binding, tipGravity)

    val popupWindow = PopupWindow(popupView, popupView.measuredWidth, popupView.measuredHeight, true).apply {
        elevation = 8f.toPx()
        showAsDropDown(anchorView)
    }
}

private fun getTipPosition(popupView: View, anchorView: View): TipPosition {
    val screenView = anchorView.rootView.findViewWithTag<View>("ROOT_VIEW")

    return if (screenView.height - anchorView.bottom > popupView.measuredHeight) {
        TipPosition.BOTTOM
    } else {
        TipPosition.TOP
    }
}

private fun setupArrow(
    popupBinding: WidgetTipViewBinding,
    tipPosition: TipPosition
) {
    val constraintLayout = popupBinding.root
    val arrowImageView = popupBinding.arrowImageView
    val cardView = popupBinding.contentCardView

    arrowImageView.rotation = if (tipPosition == TipPosition.TOP) 0f else 180f

    val startSide = if (tipPosition == TipPosition.TOP) ConstraintSet.TOP else ConstraintSet.BOTTOM
    val endSide = if (tipPosition == TipPosition.TOP) ConstraintSet.BOTTOM else ConstraintSet.TOP

    val constraintSet = ConstraintSet().apply {
        clone(constraintLayout)
        connect(arrowImageView.id, startSide, cardView.id, endSide)
        connect(arrowImageView.id, endSide, ConstraintSet.PARENT_ID, startSide)
        connect(cardView.id, endSide, arrowImageView.id, startSide)
        connect(cardView.id, startSide, ConstraintSet.PARENT_ID, endSide)
    }

    constraintSet.applyTo(constraintLayout)
}
