package com.p2p.wowlet.fragment.splashscreen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.wowlet.entities.local.SplashData
import kotlinx.android.synthetic.main.item_slpash_pager.view.*

class PagerAdapter(private val list: List<SplashData>) :
    RecyclerView.Adapter<PagerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_slpash_pager, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(data: SplashData) {
            with(itemView) {
                vTitle.text = data.title
                vHint.text = data.hint
            }
        }

    }
}