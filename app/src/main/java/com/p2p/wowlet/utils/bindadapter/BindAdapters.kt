package com.p2p.wowlet.utils.bindadapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.util.dpToPx
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

@BindingAdapter("textChangedListener")
fun AppCompatEditText.bindTextWatcher(textWatcher: TextWatcher?) {
    this.addTextChangedListener(textWatcher)
}

@BindingAdapter("image_source")
fun AppCompatImageView.imageSource(uri: String) {
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .apply(
            RequestOptions()
                .placeholder(R.drawable.bg_circule_indicator)
                .transform(RoundedCorners(38))
                .error(R.drawable.bg_circule_indicator)
        )
        .into(this)
}
@BindingAdapter("image_source_radius_16")
fun AppCompatImageView.imageSourceRadius(uri: String){
    if (uri == "") return
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .transform(RoundedCorners(16f.dpToPx().toInt()))
        .apply(
            RequestOptions()
                .placeholder(R.drawable.bg_circule_indicator)
                .error(R.drawable.bg_circule_indicator)
        )
        .into(this)
}

@BindingAdapter(value = [ "image_source_radius", "radius_dp" ])
fun AppCompatImageView.imageSourceRadiusDp(uri: String?, radius: Int){
    imageSourceRadiusDpWithDefault(uri, radius, R.drawable.bg_circule_indicator, R.drawable.bg_circule_indicator)
}


@BindingAdapter(value = [ "image_source_radius", "radius_dp", "error_drawable", "placeholder" ])
fun AppCompatImageView.imageSourceRadiusDpWithDefault(uri: String?, radius: Int, error: Int, placeHolder: Int){
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .transform(RoundedCorners(radius.toFloat().dpToPx().toInt()))
        .apply(
            RequestOptions()
                .placeholder(placeHolder)
                .error(error)
        )
        .into(this)
}



@BindingAdapter("image_source_bitmap")
fun AppCompatImageView.imageSourceBitmap(icon: Bitmap) {
    this.setImageBitmap(icon)
}
@BindingAdapter("text","symbolCount")
fun AppCompatTextView.walletFormat(address: String,symbolCount:Int){
    if(address.length>symbolCount) {
        val firstFour = address.substring(0, symbolCount)
        val stringLenght = address.length
        val lastFour = address.substring(stringLenght - symbolCount, stringLenght)
        text = "$firstFour...$lastFour"
    }
}