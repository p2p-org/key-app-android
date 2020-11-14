package com.p2p.wowlet.fragment.dashboard.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import androidx.viewpager2.widget.ViewPager2
import com.wowlet.entities.local.WalletItem
import me.relex.circleindicator.CircleIndicator3

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<WalletItem>?,
    viewModel: DashboardViewModel
) {
    data?.let {
        adapter = WalletsAdapter(viewModel, it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}

@BindingAdapter("view_pager", "view_model")
fun CircleIndicator3.bindViewPager(
    viewPager: ViewPager2,
    dashboardViewModel: DashboardViewModel
) {
    viewPager.adapter = dashboardViewModel.pages.value?.let { EnterWalletPagerAdapter(it) }
    this.setViewPager(viewPager)
}
