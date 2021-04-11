package com.p2p.wowlet.dashboard.dialog.recoveryphrase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel
import com.p2p.wowlet.domain.usecases.SecretKeyInteractor

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