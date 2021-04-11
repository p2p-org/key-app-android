package com.p2p.wowlet.utils.bindadapter

import android.graphics.Bitmap
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.ui.dialog.addcoin.util.dpToPx

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

fun AppCompatImageView.imageSourceRadiusDp(uri: String?, radius: Int) {
    imageSourceRadiusDpWithDefault(uri, radius, R.drawable.bg_circule_indicator, R.drawable.bg_circule_indicator)
}

fun AppCompatImageView.imageSourceRadiusDpWithDefault(uri: String?, radius: Int, error: Int, placeHolder: Int) {
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

fun AppCompatImageView.imageSourceBitmap(icon: Bitmap) {
    this.setImageBitmap(icon)
}

fun AppCompatTextView.walletFormat(address: String, symbolCount: Int) {
    if (address.length > symbolCount) {
        val firstFour = address.substring(0, symbolCount)
        val stringLenght = address.length
        val lastFour = address.substring(stringLenght - symbolCount, stringLenght)
        text = "$firstFour...$lastFour"
    }
}