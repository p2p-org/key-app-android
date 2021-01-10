package com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.CreateWalletInteractor
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.local.RecoveryPhraseItem

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