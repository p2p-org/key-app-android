package com.p2p.wowlet.fragment.fingetprint.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.interactors.FingerPrintInteractor
import com.p2p.wowlet.entities.local.EnableFingerPrintModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FingerPrintViewModel(private val fingerPrintInteractor: FingerPrintInteractor) : BaseViewModel() {
    private val _isSkipFingerPrint by lazy { MutableLiveData<Boolean>() }
    val isSkipFingerPrint: LiveData<Boolean> get() = _isSkipFingerPrint

    init {
        fingerPrintStatus()
    }

    fun doThisLater(isEnabled:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            fingerPrintInteractor.setFingerPrint(
                EnableFingerPrintModel(
                    isEnable = isEnabled,
                    isNotWantEnable = true
                )
            )
        }
//        _command.value =
//            NavigateNotificationViewCommand(R.id.action_navigation_fingerprint_id_to_navigation_notification)
    }

    fun enableFingerprint() {
        viewModelScope.launch(Dispatchers.IO) {
            fingerPrintInteractor.setFingerPrint(
                EnableFingerPrintModel(
                    isEnable = true,
                    isNotWantEnable = false
                )
            )
        }
//        _command.value =
//            NavigateNotificationViewCommand(R.id.action_navigation_fingerprint_id_to_navigation_notification)
    }

    fun navigateUp() {
       /* _command.value =
            NavigateUpViewCommand(R.id.action_navigation_fingerprint_id_to_navigation_enter_pin_code)*/
    }

    private fun fingerPrintStatus() {
        val data = fingerPrintInteractor.isEnableFingerPrint()
        if (data.isEnable) {
            _isSkipFingerPrint.value = data.isEnable
        } else if (data.isNotWantEnable) {
            _isSkipFingerPrint.value = data.isNotWantEnable
        }
    }
}