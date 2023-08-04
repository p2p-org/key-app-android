package org.p2p.wallet.striga.onramp.iban.adapter

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import org.p2p.wallet.R
import org.p2p.wallet.utils.getDrawableCompat

class StrigaUserIbanItemDecoration(
    context: Context,
    private val leftMarginPx: Int = 50,
    private val rightMarginPx: Int = 50
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable =
        context.getDrawableCompat(R.drawable.list_divider)!!

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        val drawCoordinateLeft = leftMarginPx
        val drawCoordinateRight = parent.width - rightMarginPx
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(child) == 0) {
                val params = child.layoutParams as RecyclerView.LayoutParams
                val drawCoordinateTop = child.bottom + params.bottomMargin
                val drawCoordinateBottom = drawCoordinateTop + divider.intrinsicHeight
                divider.setBounds(
                    drawCoordinateLeft,
                    drawCoordinateTop,
                    drawCoordinateRight,
                    drawCoordinateBottom
                )
                divider.draw(canvas)
            }
            // remove ripple effect for second item
            if (parent.getChildAdapterPosition(child) == 1) {
                child.foreground = null
            }
        }
    }
}
