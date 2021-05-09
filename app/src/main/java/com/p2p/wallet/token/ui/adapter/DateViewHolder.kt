package com.p2p.wallet.token.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.common.date.toDateString
import com.p2p.wallet.token.model.TransactionOrDateItem

class DateViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_history_date, parent, false)
) {

    private val context = parent.context
    private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

    fun onBind(operationOrDate: TransactionOrDateItem) {
        operationOrDate as TransactionOrDateItem.DateItem
        val date = operationOrDate.date
        dateTextView.text = date.toDateString(context)
    }
}