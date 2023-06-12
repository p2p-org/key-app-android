package org.p2p.uikit.utils.recycler.decoration

import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlin.math.roundToInt
import kotlin.reflect.KClass
import org.p2p.uikit.R
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.recycler.getItems
import org.p2p.uikit.utils.toPx

fun onePxDividerFinanceBlockDecoration(
    context: Context,
    orientation: Int = LinearLayout.VERTICAL,
) = OnePxDividerDecoration(MainCellModel::class, context, orientation)

class OnePxDividerDecoration(
    private val itemCellType: KClass<out AnyCellItem>,
    private val context: Context,
    orientation: Int = LinearLayout.VERTICAL,
) : MaterialDividerItemDecoration(context, orientation) {

    private val tempRect = Rect()
    private val dividerInset = 16.toPx()
    private val underlayDrawable = shapeDrawable().apply {
        fillColor = context.getColorStateList(R.color.bg_snow)
    }

    init {
        dividerThickness = 1.toPx()
        setDividerColorResource(context, R.color.elements_rain)
        isLastItemDecorated = false
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVerticalUnderlay(canvas, parent)
        super.onDraw(canvas, parent, state)
    }

    private fun drawVerticalUnderlay(canvas: Canvas, parent: RecyclerView) {
        // copy from super.drawForVerticalOrientation
        canvas.save()
        val left: Int
        val right: Int
        if (!parent.clipToPadding) {
            dividerInsetStart = parent.paddingLeft + dividerInset
            dividerInsetEnd = parent.paddingRight + dividerInset
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
        } else {
            left = 0
            right = parent.width
        }
        parent.forEach { child ->
            if (shouldDrawDivider(parent, child)) {
                parent.getDecoratedBoundsWithMargins(child, tempRect)
                val bottom = tempRect.bottom + child.translationY.roundToInt()
                val top = bottom - dividerThickness
                underlayDrawable.setBounds(left, top, right, bottom)
                underlayDrawable.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun shouldDrawDivider(position: Int, adapter: RecyclerView.Adapter<*>?): Boolean {
        adapter ?: return false
        val items = adapter.getItems()
        val item = items.getOrNull(position) ?: return false
        if (item::class != itemCellType) return false

        val nextItem = items.getOrNull(position + 1) ?: return false
        if (nextItem::class != itemCellType) return false
        return true
    }

    // copy from parent
    private fun shouldDrawDivider(parent: RecyclerView, child: View): Boolean {
        val position = parent.getChildAdapterPosition(child)
        val adapter = parent.adapter
        val isLastItem = adapter != null && position == adapter.itemCount - 1
        return (position != RecyclerView.NO_POSITION && (!isLastItem || isLastItemDecorated)
            && shouldDrawDivider(position, adapter))
    }
}
