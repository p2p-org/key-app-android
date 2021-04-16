package com.p2p.wallet.restore.secretkeys.adapter

import android.content.Context
import android.util.SparseArray
import androidx.recyclerview.widget.GridLayoutManager

class MultipleSpanGridLayoutManager(
    context: Context
) : GridLayoutManager(context, SPAN_SIZE) {

    companion object {
        const val SPAN_SIZE: Int = 52
        const val DEFAULT_SPAN_SIZE = 12
    }

    var spanSizes: SparseArray<Int> = spanSparseArray()

    init {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanSizes[position]
            }
        }
    }

    private fun spanSparseArray(): SparseArray<Int> {
        val sparseArray: SparseArray<Int> = SparseArray(12)
        sparseArray.put(0, SPAN_SIZE)
        sparseArray.put(1, SPAN_SIZE)
        sparseArray.put(2, SPAN_SIZE)
        sparseArray.put(3, SPAN_SIZE)
        sparseArray.put(4, SPAN_SIZE)
        sparseArray.put(5, SPAN_SIZE)
        sparseArray.put(6, SPAN_SIZE)
        sparseArray.put(7, SPAN_SIZE)
        sparseArray.put(8, SPAN_SIZE)
        sparseArray.put(9, SPAN_SIZE)
        sparseArray.put(10, SPAN_SIZE)
        sparseArray.put(11, SPAN_SIZE)
        sparseArray.put(12, SPAN_SIZE)
        return sparseArray
    }
}