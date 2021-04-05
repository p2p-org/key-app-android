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
    private val _getPhraseData by lazy { MutableLiveData<List<String>>() }
    val getPhraseData: LiveData<List<String>> get() = _getPhraseData

    val checkBoxIsChecked: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    init {
        generateRandomPhrase()
    }


    fun generateRandomPhrase() {
        _getPhraseData.value = createWalletInteractor.generatePhrase()
    }
}