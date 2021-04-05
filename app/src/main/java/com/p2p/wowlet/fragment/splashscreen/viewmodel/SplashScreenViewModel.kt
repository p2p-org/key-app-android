package com.p2p.wowlet.fragment.splashscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.appbase.viewmodel.SingleLiveEvent
import com.wowlet.domain.interactors.SplashScreenInteractor
import com.wowlet.entities.local.SplashData

class SplashScreenViewModel(
    val splashScreenInteractor: SplashScreenInteractor
) : BaseViewModel() {

    private val _pages: MutableLiveData<List<SplashData>> by lazy { MutableLiveData() }
    val pages: LiveData<List<SplashData>> get() = _pages

    val launchPinCode = SingleLiveEvent<Boolean>()

    init {
        if (splashScreenInteractor.isAuthorized()) {
            launchPinCode.value = true
        }
    }

    fun initData(list: List<SplashData>) {
        _pages.value = list
    }
}