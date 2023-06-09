package org.p2p.wallet.common.ui.recycler.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R

class DividerItemDecorator(
    context: Context,
    @DrawableRes dividerDrawableRes: Int = R.drawable.list_divider
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable = context.getDrawable(dividerDrawableRes)!!

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 1..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + divider.intrinsicHeight
            divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            divider.draw(canvas)
        }
    }
}
