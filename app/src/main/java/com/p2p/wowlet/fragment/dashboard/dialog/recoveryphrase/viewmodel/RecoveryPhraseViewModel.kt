package com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.RecoveryPhraseItem

class RecoveryPhraseViewModel : BaseViewModel() {
    private val listSortData = mutableListOf(
        RecoveryPhraseItem(1, "1.phrase"),
        RecoveryPhraseItem(2, "2.phrase"),
        RecoveryPhraseItem(3, "3.phrase"),
        RecoveryPhraseItem(4, "4.phrase"),
        RecoveryPhraseItem(5, "5.phrase"),
        RecoveryPhraseItem(6, "6.phrase")
    )

    private val _getSortSecretData by lazy { MutableLiveData<MutableList<RecoveryPhraseItem>>() }
    val getSortSecretData: LiveData<MutableList<RecoveryPhraseItem>> get() = _getSortSecretData
   private val _dismissDialog by lazy { MutableLiveData<Unit>() }
    val dismissDialog: LiveData<Unit> get() = _dismissDialog

    init {
        _getSortSecretData.value = listSortData
    }

    fun dismissDialog(){

    }
}