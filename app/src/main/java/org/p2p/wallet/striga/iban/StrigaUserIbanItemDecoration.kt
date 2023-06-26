package org.p2p.wallet.striga.iban

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import org.p2p.wallet.R
import org.p2p.wallet.utils.getDrawableCompat

class StrigaUserIbanItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider: Drawable =
        context.getDrawableCompat(R.drawable.list_divider)!!

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        val drawCoordinateLeft = 50
        val drawCoordinateRight = parent.width - 50
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
