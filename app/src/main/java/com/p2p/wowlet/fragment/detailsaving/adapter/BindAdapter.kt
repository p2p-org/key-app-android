package com.p2p.wowlet.fragment.detailsaving.adapter

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.google.android.material.tabs.TabLayout
import com.wowlet.entities.local.ActivityItem


@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<ActivityItem>?,
    viewModel: DetailSavingViewModel
) {
    data?.let {
        adapter = ActivityDetailAdapter(viewModel, it)
    }
    this.layoutManager = LinearLayoutManager(context)
}

@BindingAdapter("tab_item_selected")
fun TabLayout.tabItem(viewModel: DetailSavingViewModel) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                setTypeface(typeface, Typeface.BOLD)
                background =
                    ContextCompat.getDrawable(context, R.drawable.bg_detail_selected_tab)
            }}

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
                background =
                    ContextCompat.getDrawable(context, R.drawable.bg_detail_unselected_tab)
                setTypeface(typeface, Typeface.NORMAL)
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            tab?.customView?.findViewById<TextView>(R.id.tab)?.apply {
               setTypeface(typeface, Typeface.BOLD)
                background =
                    ContextCompat.getDrawable(context, R.drawable.bg_detail_selected_tab)
            }}

    })
}