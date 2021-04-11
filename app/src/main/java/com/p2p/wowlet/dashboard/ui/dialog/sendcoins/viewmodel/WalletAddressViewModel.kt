package com.p2p.wowlet.dashboard.ui.dialog.sendcoins.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.p2p.wowlet.dashboard.model.local.QrWalletType

class WalletAddressViewModel : ViewModel() {

    private val _walletData by lazy { MutableLiveData<QrWalletType>() }
    val walletData: LiveData<QrWalletType> get() = _walletData

    private val _enteredAmount by lazy { MutableLiveData<String>() }
    val enteredAmountLV: LiveData<String> get() = _enteredAmount

    var disableObserving = false
    var isUserToggledCurrencyType = false
    var enteredAmount: String = ""

    fun setWalletData(data: QrWalletType) {
        disableObserving = false
        _walletData.value = data
    }

    fun postEnteredAmount() {
        _enteredAmount.value = enteredAmount
    }
}