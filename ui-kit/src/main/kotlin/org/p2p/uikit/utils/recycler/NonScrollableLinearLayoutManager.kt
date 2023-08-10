package org.p2p.uikit.utils.recycler

import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context

class NonScrollableLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    override fun canScrollHorizontally(): Boolean = false
    override fun canScrollVertically(): Boolean = false
}
