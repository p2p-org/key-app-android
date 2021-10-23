package org.p2p.wallet.history.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toDateString
import org.p2p.wallet.history.model.HistoryItem

class DateViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_history_date, parent, false)
) {

    private val context = parent.context
    private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

    fun onBind(operationOrDate: HistoryItem) {
        operationOrDate as HistoryItem.DateItem
        val date = operationOrDate.date
        dateTextView.text = date.toDateString(context)
    }
}