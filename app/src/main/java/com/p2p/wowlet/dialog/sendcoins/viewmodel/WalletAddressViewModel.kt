package com.p2p.wowlet.dialog.sendcoins.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WalletAddressViewModel : ViewModel() {

    private val _walletAddress by lazy { MutableLiveData<String>() }
    val walletAddress: LiveData<String> get() = _walletAddress

    var disableObserving = false
    var enteredAmount: String = ""

    fun setWalletAddress(walletAddress: String) {
        disableObserving = false
        _walletAddress.value = walletAddress
    }

}