package com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter

import android.content.Context
import android.util.SparseArray
import androidx.recyclerview.widget.GridLayoutManager

class MultipleSpanGridLayoutManager(
    context: Context
) : GridLayoutManager(context, SPAN_SIZE) {

    companion object {
        const val SPAN_SIZE: Int = 52
    }

    var spanSizes: SparseArray<Int> = spanSparseArray()

    init {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanSizes[position]
            }
        }
    }



    private fun spanSparseArray() : SparseArray<Int> {
        val sparseArray: SparseArray<Int> = SparseArray(12)
        sparseArray.put(0,10)
        sparseArray.put(1,10)
        sparseArray.put(2,10)
        sparseArray.put(3,10)
        sparseArray.put(4,10)
        sparseArray.put(5,10)
        sparseArray.put(6,10)
        sparseArray.put(7,10)
        sparseArray.put(8,10)
        sparseArray.put(9,10)
        sparseArray.put(10,10)
        sparseArray.put(11,10)
        sparseArray.put(12,10)
        return sparseArray
    }
}