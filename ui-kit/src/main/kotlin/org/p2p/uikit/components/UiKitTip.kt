package org.p2p.uikit.components

import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetTipViewBinding
import org.p2p.uikit.utils.toPx

private const val POPUP_WINDOW_ELEVATION_DP = 8F

private enum class TipPosition {
    TOP,
    BOTTOM
}

/**
 * Shows PopupWindow with arrow
 * PopupWindow appears natively above or under the view
 * Arrow is shown only on top or bottom of PopupWindow
 * Arrow is pointing on the center of view
 */
fun View.showTip(
    counterText: String,
    @StringRes title: Int,
    @StringRes buttonNextTitle: Int = R.string.tip_button_next_title,
    @StringRes buttonSkipTitle: Int = R.string.tip_button_skip_all_title,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    showTip(
        counterText = counterText,
        title = context.getString(title),
        buttonNextTitle = context.getString(buttonNextTitle),
        buttonSkipTitle = context.getString(buttonSkipTitle),
        onNextClick = onNextClick,
        onSkipClick = onSkipClick
    )
}

/**
 * Shows PopupWindow with arrow
 * PopupWindow appears natively above or under the view
 * Arrow is shown only on top or bottom of PopupWindow
 * Arrow is pointing on the center of view
 */
fun View.showTip(
    counterText: String,
    title: String,
    buttonNextTitle: String,
    buttonSkipTitle: String,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val binding = WidgetTipViewBinding.inflate(LayoutInflater.from(context)).apply {
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

    val popupWindow = PopupWindow(
        popupView,
        popupView.measuredWidth,
        popupView.measuredHeight,
        true
    ).apply {
        elevation = POPUP_WINDOW_ELEVATION_DP.toPx()
        showAsDropDown(this@showTip)
    }.apply {
        contentView.post {
            val tipGravity = getTipPosition(popupView, this@showTip)
            val arrowMargin = calculateArrowMargin(popupView, this@showTip, binding.arrowImageView)
            setupArrow(binding, tipGravity, arrowMargin)
        }
    }
}

/**
 * Returns [popupView] position relatively to [anchorView]
 * Should be called after popupView is drawn!
 */
private fun getTipPosition(popupView: View, anchorView: View): TipPosition {
    val popupLocation = popupView.getLocationOnScreen()
    val anchorLocation = anchorView.getLocationOnScreen()

    return if (popupLocation.y > anchorLocation.y) {
        TipPosition.BOTTOM
    } else {
        TipPosition.TOP
    }
}

/**
 * Set arrow position depending on [tipPosition]
 * Set arrow margin
 */
private fun setupArrow(
    popupBinding: WidgetTipViewBinding,
    tipPosition: TipPosition,
    arrowMargin: Int
) {
    val constraintLayout = popupBinding.root
    val arrowImageView = popupBinding.arrowImageView
    val cardView = popupBinding.contentCardView

    arrowImageView.apply {
        rotation = if (tipPosition == TipPosition.TOP) 0f else 180f
        updateLayoutParams<ConstraintLayout.LayoutParams> { marginStart = arrowMargin }
    }

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

/**
 * Evaluate [arrowImageView] margin to set it right in the middle of [anchorView]
 */
private fun calculateArrowMargin(
    popupView: View,
    anchorView: View,
    arrowImageView: View
): Int {
    val minMargin = popupView.resources.getDimension(R.dimen.ui_kit_tip_corner_radius).toInt()
    val maxMargin = popupView.width - minMargin - arrowImageView.width

    val popupViewLocation = popupView.getLocationOnScreen()
    val anchorViewLocation = anchorView.getLocationOnScreen()

    val popupLeft = popupViewLocation.x
    val anchorLeft = anchorViewLocation.x

    // anchorView.width / 2 is added to center the arrowImageView
    val anchorMid = anchorLeft + anchorView.width / 2
    val anchorShiftedMid = anchorMid - popupLeft
    val margin = anchorShiftedMid - arrowImageView.width / 2

    // Arrow margin should not be less then minMargin and bigger then maxMargin to not go beyond popup corner radius
    return margin.coerceIn(minMargin, maxMargin)
}

private fun View.getLocationOnScreen(): Point {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Point(location[0], location[1])
}
