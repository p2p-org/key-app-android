package com.p2p.wallet.dashboard.model.local

import androidx.annotation.DrawableRes

data class UserWalletType(
    val walletType: String,
    val userContact: String,
    val isContact: Boolean,
    @DrawableRes val userType: Int
)