package com.p2p.wowlet.fragment.createwallet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.CreateWalletInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateWalletViewModel(private val createWalletInteractor: CreateWalletInteractor) :
    BaseViewModel() {
    private val _getPhraseData by lazy { MutableLiveData<String>() }
    val getPhraseData: LiveData<String> get() = _getPhraseData

    val checkBoxIsChecked: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    init {
        generateRandomPhrase()
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_create_wallet_to_navigation_reg_login)
    }

    fun goToRegWalletFragment() {
        viewModelScope.launch(Dispatchers.IO) {
            createWalletInteractor.initUser()
            withContext(Dispatchers.Main) {
                _command.value =
                    Command.NavigateRegWalletViewCommand(R.id.action_navigation_create_wallet_to_navigation_reg_wallet)
            }
        }
    }

    private fun generateRandomPhrase() {
        _getPhraseData.value = createWalletInteractor.generatePhrase()
    }
}