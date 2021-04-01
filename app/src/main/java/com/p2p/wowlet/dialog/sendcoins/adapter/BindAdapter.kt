package com.p2p.wowlet.dialog.sendcoins.adapter

import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import me.relex.circleindicator.CircleIndicator3


@BindingAdapter("view_pager", "view_model")
fun CircleIndicator3.bindViewPager(
    viewPager: ViewPager2,
    sendCoinsViewModel: SendCoinsViewModel
) {
    viewPager.adapter = sendCoinsViewModel.pages.value?.let { PagerAdapter(it, sendCoinsViewModel) }
    this.setViewPager(viewPager)
}