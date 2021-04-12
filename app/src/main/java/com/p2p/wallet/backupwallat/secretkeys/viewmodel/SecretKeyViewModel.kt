package com.p2p.wallet.backupwallat.secretkeys.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel
import com.p2p.wallet.backupwallat.interactor.SecretKeyInteractor
import com.p2p.wallet.common.network.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecretKeyViewModel(
    private val secretKeyInteractor: SecretKeyInteractor
) : BaseViewModel() {

    private val _isCurrentCombination by lazy { MutableLiveData<Boolean>() }
    val isCurrentCombination: LiveData<Boolean> get() = _isCurrentCombination
    private val _invadedPhrase by lazy { MutableLiveData<String>() }
    val invadedPhrase: LiveData<String> get() = _invadedPhrase
    private val _shouldResetThePhrase: MutableLiveData<Boolean> = MutableLiveData()
    val shouldResetThePhrase: LiveData<Boolean> get() = _shouldResetThePhrase

    val phrase: MutableLiveData<String> = MutableLiveData("")

    fun verifyPhrase(phrase: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = secretKeyInteractor.resetPhrase(phrase)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _isCurrentCombination.value = data.data
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _invadedPhrase.value = data.errors.errorMessage
                }
            }
        }
    }

    fun resetPhrase() {
        _shouldResetThePhrase.value = true
    }

    fun postInvadedPhrase(errorMessage: String) {
        _invadedPhrase.value = errorMessage
    }
}