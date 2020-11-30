package com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.wowlet.domain.interactors.SecretKeyInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecretKeyViewModel(val secretKeyInteractor: SecretKeyInteractor) : BaseViewModel() {

    val phrase: MutableLiveData<String> by lazy { MutableLiveData() }

    private val _isCurrentCombination by lazy { MutableLiveData<Boolean>() }
    val isCurrentCombination: LiveData<Boolean> get() = _isCurrentCombination

    fun goToPinCodeFragment() {
        _command.value =
            Command.NavigatePinCodeViewCommand(
                R.id.action_navigation_secret_key_to_navigation_pin_code,
                bundleOf(
                    PinCodeFragment.OPEN_FRAGMENT_SPLASH_SCREEN to false,
                    PinCodeFragment.CREATE_NEW_PIN_CODE to true)
            )
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_secret_key_to_navigation_recovery_wallet)
    }

    fun resetPhrase(phrase: String) {
        viewModelScope.launch (Dispatchers.IO){
            val data= secretKeyInteractor.resetPhrase(phrase)
            withContext(Dispatchers.Main){
                _isCurrentCombination.value = data
            }
        }
    }

}