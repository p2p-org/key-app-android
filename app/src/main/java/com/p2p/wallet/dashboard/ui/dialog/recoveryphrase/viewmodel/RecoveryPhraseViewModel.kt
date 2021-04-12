package com.p2p.wallet.dashboard.ui.dialog.recoveryphrase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel
import com.p2p.wallet.backupwallat.interactor.SecretKeyInteractor

class RecoveryPhraseViewModel(val secretKeyInteractor: SecretKeyInteractor) :
    BaseViewModel() {

    private val _getSortSecretData by lazy { MutableLiveData<List<String>>() }
    val getSortSecretData: LiveData<List<String>> get() = _getSortSecretData
    private val _dismissDialog by lazy { MutableLiveData<Unit>() }
    val dismissDialog: LiveData<Unit> get() = _dismissDialog

    init {
        _getSortSecretData.value = secretKeyInteractor.currentListPhrase()
    }
}