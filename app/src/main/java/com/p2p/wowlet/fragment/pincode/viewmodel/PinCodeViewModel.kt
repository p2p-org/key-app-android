package com.p2p.wowlet.fragment.pincode.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.PinCodeInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinCodeViewModel(private val pinCodeInteractor: PinCodeInteractor) : BaseViewModel() {

    private val _checkPinCode by lazy { MutableLiveData<Boolean>() }
    val checkPinCode: LiveData<Boolean> get() = _checkPinCode

    fun goToFaceIdFragment() {
        _command.value =
            NavigateFaceIdViewCommand(R.id.action_navigation_pin_code_to_navigation_face_id)
    }

    fun navigateUp() {
        _command.value =
            NavigateUpViewCommand(R.id.action_navigation_pin_code_to_navigation_reg_wallet)
    }

    fun initCode(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentPinCode = pinCodeInteractor.initPinCode(value)
            withContext(Dispatchers.Main) {
                _checkPinCode.value = isCurrentPinCode
            }
        }
    }

}