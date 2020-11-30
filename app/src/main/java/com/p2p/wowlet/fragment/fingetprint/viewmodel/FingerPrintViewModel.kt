package com.p2p.wowlet.fragment.fingetprint.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.FingerPrintInteractor
import com.wowlet.entities.local.EnableFingerPrintModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FingerPrintViewModel(private val fingerPrintInteractor: FingerPrintInteractor) : BaseViewModel() {
    private val _isSkipFingerPrint by lazy { MutableLiveData<Unit>() }
    val isSkipFingerPrint: LiveData<Unit> get() = _isSkipFingerPrint

    init {
        fingerPrintStatus()
    }

    fun doThisLater() {
        viewModelScope.launch(Dispatchers.IO) {
            fingerPrintInteractor.setFingerPrint(
                EnableFingerPrintModel(
                    isEnable = false,
                    isNotWantEnable = true
                )
            )
        }
        _command.value =
            NavigateNotificationViewCommand(R.id.action_navigation_fingerprint_id_to_navigation_notification)
    }

    fun navigateUp() {
       /* _command.value =
            NavigateUpViewCommand(R.id.action_navigation_fingerprint_id_to_navigation_enter_pin_code)*/
    }

    private fun fingerPrintStatus() {
        val data = fingerPrintInteractor.isEnableFingerPrint()
        if (data.isEnable) {
            _isSkipFingerPrint.value = Unit
        } else if (data.isNotWantEnable) {
            _isSkipFingerPrint.value = Unit
        }
    }
}