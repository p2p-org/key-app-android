package org.p2p.wallet.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import timber.log.Timber

class HomeScreenLayoutManager(context: Context) : LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(e, "Inconsistency detected")
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}
