package org.p2p.wallet.striga.signup.presetpicker.adapter

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import org.p2p.uikit.components.finance_block.UiKitMainCellView
import org.p2p.uikit.organisms.sectionheader.UiKitSectionHeader
import org.p2p.uikit.utils.dip
import org.p2p.wallet.R

class StrigaPresetPickItemDecorator(
    context: Context,
    @DrawableRes dividerDrawableRes: Int = R.drawable.list_divider_smoke
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable = context.getDrawable(dividerDrawableRes)!!

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - dip(16)
        val childCount = parent.childCount

        for (i in 1..childCount - 2) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)
            val previousChild = parent.getChildAt(i - 1)

            if (child is UiKitSectionHeader && previousChild is UiKitMainCellView && nextChild is UiKitMainCellView) {
                continue
            } else if (child is UiKitMainCellView && previousChild is UiKitSectionHeader &&
                nextChild is UiKitSectionHeader
            ) {
                continue
            } else {
                val dividerTop = child.bottom - divider.intrinsicHeight
                val dividerBottom = child.bottom
                divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                divider.draw(canvas)
            }
        }
    }
}
