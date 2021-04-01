package com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.wowlet.domain.interactors.ManualSecretKeyInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.enums.PinCodeFragmentType
import com.wowlet.entities.local.SecretKeyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManualSecretKeyViewModel(private val manualSecretKeyInteractor: ManualSecretKeyInteractor) :
    BaseViewModel() {

    private val _getPhraseData by lazy { MutableLiveData<SecretKeyItem>() }
    val getPhraseData: LiveData<SecretKeyItem> get() = _getPhraseData
    private val _resultResponseData by lazy { MutableLiveData<Boolean>() }
    val resultResponseData: LiveData<Boolean> get() = _resultResponseData

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_secret_key_to_navigation_manual_secret_key)
    }

    fun goToPinCodeFragment() {
        _command.value =
            Command.NavigatePinCodeViewCommand(
                R.id.action_navigation_manual_secret_key_to_navigation_pin_code,
                bundleOf(
                    PinCodeFragment.OPEN_FRAGMENT_SPLASH_SCREEN to false,
                    PinCodeFragment.CREATE_NEW_PIN_CODE to PinCodeFragmentType.CREATE
                )
            )
    }

    fun randomItemClickListener(secretKeyItem: SecretKeyItem) {
        getResult(secretKeyItem)
        _getPhraseData.value = secretKeyItem
    }

    private fun getResult(secretKeyItem: SecretKeyItem) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = manualSecretKeyInteractor.sortingSecretKey(secretKeyItem)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _resultResponseData.value = data.data
                }
            }
        }
    }
}