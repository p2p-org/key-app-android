package com.p2p.wowlet.auth.ui.createwallet.viewmodel

import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.auth.interactor.CreateWalletInteractor
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel

class CreateWalletViewModel(
    private val createWalletInteractor: CreateWalletInteractor
) : BaseViewModel() {

    val phrasesLiveData = MutableLiveData<List<String>>()

    fun generatePhrases() {
        phrasesLiveData.value = createWalletInteractor.generatePhrase()
    }
}