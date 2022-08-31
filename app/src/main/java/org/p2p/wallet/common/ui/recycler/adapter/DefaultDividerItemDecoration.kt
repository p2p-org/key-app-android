package org.p2p.wallet.common.ui.recycler.adapter

import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import org.p2p.wallet.R

class DefaultDividerItemDecoration(context: Context, orientation: Int) : DividerItemDecoration(context, orientation) {
    init {
        context.getDrawable(R.drawable.list_divider)?.let {
            setDrawable(it)
        }
    }
}
