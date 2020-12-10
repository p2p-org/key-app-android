package com.p2p.wowlet.utils.bindadapter

import android.graphics.Bitmap
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

@BindingAdapter("textChangedListener")
fun AppCompatEditText.bindTextWatcher(textWatcher: TextWatcher?) {
    this.addTextChangedListener(textWatcher)
}
@BindingAdapter("image_source")
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
@BindingAdapter("image_source_bitmap")
fun AppCompatImageView.imageSourceBitmap(icon: Bitmap){
    this.setImageBitmap(icon)
}
@BindingAdapter("text")
fun AppCompatTextView.walletFormat(address: String){
    if(address.length>4) {
        val firstFour = address.substring(0, 4)
        val stringLenght = address.length
        val lastFour = address.substring(stringLenght - 4, stringLenght)
        text = "0x$firstFour...$lastFour"
    }
}



