package com.p2p.wowlet.fragment.pincode.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment.Companion.BACK_TO_DASHBOARD_SCREEN
import com.wowlet.domain.interactors.FingerPrintInteractor
import com.wowlet.domain.interactors.NotificationInteractor
import com.wowlet.domain.interactors.PinCodeInteractor
import com.wowlet.entities.Constants.Companion.VERIFY_PIN_CODE_ERROR
import com.wowlet.entities.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinCodeViewModel(
    private val pinCodeInteractor: PinCodeInteractor,
    private val fingerPrintInteractor: FingerPrintInteractor,
    private val notificationInteractor: NotificationInteractor
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
        val data = fingerPrintInteractor.isEnableFingerPrint()
        _isSkipFingerPrint.value = data.isEnable
    }

    fun notificationStatus() {
        val data = notificationInteractor.isEnableNotification()
        when {
            data.isEnable -> {
                _skipNotification.value = true
            }
            data.isNotWantEnable -> {
                _skipNotification.value = true
            }
            else -> {
                _skipNotification.value = false
            }
        }
    }

    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateRegLoginViewCommand(R.id.action_navigation_pin_code_to_navigation_reg_login)
    }

    fun goToSecretKeyFragment() {
        _command.value =
            Command.NavigateSecretKeyViewCommand(R.id.action_navigation_pin_code_to_navigation_secret_key)
    }

    fun goToRegFinishFragment() {
        _command.value =
            Command.NavigateRegFinishViewCommand(R.id.action_navigation_pin_code_to_navigation_reg_finish)
    }

    fun goToNotificationFragment() {
        _command.value =
            Command.NavigateNotificationViewCommand(R.id.action_navigation_pin_code_to_navigation_notification)
    }

    fun goToFingerPrintFragment() {
        _command.value =
            Command.NavigateFingerPrintViewCommand(R.id.action_navigation_pin_code_to_navigation_fingerprint_id)
    }

    fun goBackToDashboardFragment(isSuccess: Boolean?) {
        _command.value =
            Command.NavigateDashboardViewCommand(
                R.id.action_navigation_pin_code_to_navigation_dashboard,
                bundleOf(BACK_TO_DASHBOARD_SCREEN to isSuccess)
            )
    }

    fun initCode(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = pinCodeInteractor.initPinCode(value)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _pinCodeSaved.value = Unit
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    if (data.errors.errorCode == VERIFY_PIN_CODE_ERROR) {
                        _verifyPinCodeError.value = true
                    } else {
                        _pinCodeError.value = data.errors.errorMessage
                    }
                }
            }
        }
    }

    fun verifyPinCode(value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = pinCodeInteractor.verifyPinCode(value)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    data.data?.let {
                        if (it) {
                            _pinCodeSuccess.value = Unit
                        } else {
                            _verifyPinCodeError.value = true
                        }
                    }

                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _verifyPinCodeError.value = true
                }
            }
        }
    }

    fun openFingerprintDialog() {
        _openFingerprintDialog.value = true
    }
}