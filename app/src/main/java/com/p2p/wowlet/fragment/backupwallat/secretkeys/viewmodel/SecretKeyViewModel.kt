package com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.local.SecretKeyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecretKeyViewModel(val secretKeyInteractor: SecretKeyInteractor) : BaseViewModel() {

    private var listSortData = listOf<SecretKeyItem>()

    private val _getRandomSecretData by lazy { MutableLiveData<MutableList<SecretKeyItem>>() }
    val getRandomSecretData: LiveData<MutableList<SecretKeyItem>> get() = _getRandomSecretData

    private val _getSortSecretData by lazy { MutableLiveData<List<SecretKeyItem>>() }
    val getSortSecretData: LiveData<List<SecretKeyItem>> get() = _getSortSecretData
    private val _getCurrentCombination by lazy { MutableLiveData<Boolean>() }
    val getCurrentCombination: LiveData<Boolean> get() = _getCurrentCombination

    init {
        initSortSecretData()
    }

    fun goToCompleteWalletFragment() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_secret_key_to_navigation_complete_wallet)
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_secret_key_to_navigation_recovery_wallet)
    }

    fun selectItem(item: SecretKeyItem) {

        listSortData.forEach {
            if (it.id == item.id) {
                it.selected = true

            }
        }
        _getSortSecretData.value = listSortData
        val secretKeyCombinationSuccess = secretKeyInteractor.checkCurrentSelected(item.id)
        if (secretKeyCombinationSuccess.selectedItemCount == 3) {
            _getCurrentCombination.value = secretKeyCombinationSuccess.isCurrentCombination
        }

    }

    private fun initSortSecretData() {
        viewModelScope.launch(Dispatchers.IO) {

            listSortData = secretKeyInteractor.getSecretData()
            withContext(Dispatchers.Main) {
                _getSortSecretData.value = listSortData
            }
        }
    }

    fun resetSelectData() {
        listSortData.forEach {
                it.selected = false
        }
        _getSortSecretData.value = listSortData
    }


}