package com.p2p.wallet.utils.bindadapter

import android.graphics.Bitmap
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.p2p.wallet.dashboard.ui.dialog.addcoin.util.dpToPx

fun AppCompatImageView.imageSource(uri: String) {
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .apply(
            RequestOptions()
                .transform(RoundedCorners(38))
        )
        .into(this)
}

fun AppCompatImageView.imageSourceRadiusDp(uri: String?, radius: Int) {
    imageSourceRadiusDpWithDefault(uri, radius)
}

fun AppCompatImageView.imageSourceRadiusDpWithDefault(uri: String?, radius: Int) {
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .transform(RoundedCorners(radius.toFloat().dpToPx().toInt()))
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