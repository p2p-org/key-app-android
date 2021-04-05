package com.p2p.wowlet.fragment.createwallet.viewmodel

import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.CreateWalletInteractor

class CreateWalletViewModel(
    private val createWalletInteractor: CreateWalletInteractor
) : BaseViewModel() {

    val phrasesLiveData = MutableLiveData<List<String>>()

    fun generatePhrases() {
        phrasesLiveData.value = createWalletInteractor.generatePhrase()
    }
}