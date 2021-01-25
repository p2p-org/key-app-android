package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NoScrollLinearLayoutManager(
    context: Context
) : LinearLayoutManager(context) {

    private var isScrollable = true

    fun enableScrolling() {
        isScrollable = true
    }

    fun disableScrolling() {
        isScrollable = false
    }

    override fun canScrollVertically(): Boolean {
        return super.canScrollVertically() && isScrollable
    }

    override fun canScrollHorizontally(): Boolean {
        return super.canScrollHorizontally() && isScrollable
    }
}