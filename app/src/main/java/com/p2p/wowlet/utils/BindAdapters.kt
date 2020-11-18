package com.p2p.wowlet.utils

import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.p2p.wowlet.R
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

@BindingAdapter("imageSource")
fun AppCompatImageView.imageSource(uri: String){
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .apply(
            RequestOptions()
                .placeholder(R.drawable.bg_circule_indicator)
                .error(R.drawable.bg_circule_indicator)
        )
        .into(this)
}

