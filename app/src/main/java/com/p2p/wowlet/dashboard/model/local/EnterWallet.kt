package com.p2p.wowlet.dashboard.model.local

import android.graphics.Bitmap

data class EnterWallet(
    val qrCode: Bitmap,
    val walletAddress: String,
    val icon: String,
    val name: String
)