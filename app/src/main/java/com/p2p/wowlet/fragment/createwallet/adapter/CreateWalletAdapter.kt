package com.p2p.wowlet.fragment.createwallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import kotlinx.android.synthetic.main.item_security_key.view.*

class CreateWalletAdapter(private var phraseList: List<String>) :
    RecyclerView.Adapter<CreateWalletAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(value: String) {
            with(itemView) {
                itemText.text = value
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.item_security_key, parent, false)
        return MyViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(phraseList[position])
    }

    override fun getItemCount(): Int = phraseList.size

    fun setData(mutableList: List<String>) {
        phraseList = mutableList
        notifyDataSetChanged()
    }
}