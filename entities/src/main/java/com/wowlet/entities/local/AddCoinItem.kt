package com.wowlet.entities.local

import androidx.lifecycle.MutableLiveData

data class AddCoinItem(
    val tokenSymbol: String,
    val mintAddress: String,
    val tokenName: String,
    val icon: String,
    val change24hPrice: Double,
    val change24hPercentages: Double,
    val currency: Double,
    var isShowMindAddress: Boolean=false,
    var isAlreadyAdded: Boolean = false,
    var navigatingBack: Boolean = false
)