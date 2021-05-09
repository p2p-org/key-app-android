package com.p2p.wallet.utils.bindadapter

import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide

fun AppCompatImageView.imageSourceRadiusDp(uri: String?, radius: Int) {
    imageSourceRadiusDpWithDefault(uri, radius)
}

fun AppCompatImageView.imageSourceRadiusDpWithDefault(uri: String?, radius: Int) {
    Glide.with(context)
        .load(uri)
        .centerCrop()
        .into(this)
}