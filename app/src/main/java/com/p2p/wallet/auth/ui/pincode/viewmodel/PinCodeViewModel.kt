package com.p2p.wallet.auth.ui.pincode.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel

class PinCodeViewModel(
//    private val notificationInteractor: NotificationInteractor
) : BaseViewModel() {

    private val _pinCodeSuccess by lazy { MutableLiveData<Unit>() }
    val pinCodeSuccess: LiveData<Unit> get() = _pinCodeSuccess

    private val _verifyPinCodeError by lazy { MutableLiveData<Boolean>() }
    val verifyPinCodeError: LiveData<Boolean> get() = _verifyPinCodeError

    private val _pinCodeSaved by lazy { MutableLiveData<Unit>() }
    val pinCodeSaved: LiveData<Unit> get() = _pinCodeSaved

    private val _pinCodeError by lazy { MutableLiveData<String>() }
    val pinCodeError: LiveData<String> get() = _pinCodeError

    private val _isSkipFingerPrint by lazy { MutableLiveData<Boolean>() }
    val isSkipFingerPrint: LiveData<Boolean> get() = _isSkipFingerPrint
    private val _skipNotification by lazy { MutableLiveData<Boolean>() }
    val skipNotification: LiveData<Boolean> get() = _skipNotification

    private val _openFingerprintDialog by lazy { MutableLiveData<Boolean>() }
    val openFingerprintDialog: LiveData<Boolean> get() = _openFingerprintDialog

    fun fingerPrintStatus() {
//        val data = fingerPrintInteractor.isEnableFingerPrint()
//        _isSkipFingerPrint.value = data.isEnable
    }

    fun notificationStatus() {
//        val data = notificationInteractor.isEnableNotification()
//        when {
//            data.isEnable -> {
//                _skipNotification.value = true
//            }
//            data.isNotWantEnable -> {
//                _skipNotification.value = true
//            }
//            else -> {
//                _skipNotification.value = false
//            }
//        }
    }

    fun openFingerprintDialog() {
        _openFingerprintDialog.value = true
    }
}