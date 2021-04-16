package com.p2p.wallet.restore.secretkeys.utils

import android.view.View
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.restore.secretkeys.adapter.MultipleSpanGridLayoutManager
import kotlin.math.roundToInt

class MeasureHelper(
    private val recyclerView: RecyclerView?,
    private val rvItem: View
) {

    fun getSpanSize(): Int {
        rvItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val rvItemViewSize: Float = (
            rvItem.measuredWidth +
                rvItem.marginStart +
                rvItem.marginEnd +
                rvItem.paddingStart +
                rvItem.paddingEnd
            ).toFloat()
        val rvSize: Float = (recyclerView?.width ?: 1).toFloat()
        val percentRatio: Float = rvItemViewSize.div(rvSize)
        val spanSize: Float = MultipleSpanGridLayoutManager.SPAN_SIZE * percentRatio
        return spanSize.roundToInt() - 1
    }
}