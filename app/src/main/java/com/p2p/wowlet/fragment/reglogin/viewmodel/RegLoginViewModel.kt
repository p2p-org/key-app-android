package com.p2p.wowlet.fragment.reglogin.viewmodel

import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.RegLoginInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegLoginViewModel(val regLoginInteractor: RegLoginInteractor) : BaseViewModel() {
    private var phraseList = listOf<String>()

    init {
        initSecretUserData()
    }

    fun goToRecoveryWalletFragment() {
        _command.value =
            Command.NavigateRecoveryWalletViewCommand(R.id.action_navigation_reg_login_to_navigation_recovery_wallet,phraseList)
    }

    fun goToRegWalletFragment() {
        _command.value =
            Command.NavigateRegWalletViewCommand(R.id.action_navigation_reg_login_to_navigation_reg_wallet)
    }

    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }

    private fun initSecretUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            phraseList = regLoginInteractor.initUser().phrase
        }

    }

}