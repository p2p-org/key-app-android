package com.p2p.wowlet.utils

import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.p2p.wowlet.fragment.splashscreen.adapters.PagerAdapter
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import me.relex.circleindicator.CircleIndicator3

@BindingAdapter("view_pager", "view_model")
fun CircleIndicator3.bindViewPager(
    viewPager: ViewPager2,
    splashViewModel: SplashScreenViewModel
) {
    viewPager.adapter = splashViewModel.pages.value?.let { PagerAdapter(it) }
    this.setViewPager(viewPager)
}

