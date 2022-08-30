package org.p2p.wallet.common.ui.recycler.adapter

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import org.p2p.wallet.R

class DefaultDividerItemDecoration(context: Context, orientation: Int) : DividerItemDecoration(context, orientation) {
    init {
        AppCompatResources.getDrawable(context, R.drawable.list_divider)?.let {
            setDrawable(it)
        }
    }
}
