package com.p2p.wowlet.fragment.splashscreen.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment.Companion.CREATE_NEW_PIN_CODE
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment.Companion.OPEN_FRAGMENT_SPLASH_SCREEN

import com.wowlet.domain.interactors.SplashScreenInteractor
import com.wowlet.entities.local.SplashData

class SplashScreenViewModel(val splashScreenInteractor: SplashScreenInteractor) : BaseViewModel() {

    private val _pages: MutableLiveData<List<SplashData>> by lazy { MutableLiveData() }
    val pages: LiveData<List<SplashData>> get() = _pages

    init {
        if (splashScreenInteractor.isCurrentLoginReg()) {
            goToPinCodeFragment()
        }
    }

    fun initData(list: List<SplashData>) {
        _pages.value = list
    }

    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }

    fun goToRegLoginFragment() {
        _command.value =
            Command.NavigateRegLoginViewCommand(R.id.action_navigation_splash_screen_to_navigation_reg_login)
    }

    private fun goToPinCodeFragment() {
        _command.value =
            Command.NavigatePinCodeViewCommand(
                R.id.action_navigation_splash_screen_to_navigation_pin_code,
                bundleOf(OPEN_FRAGMENT_SPLASH_SCREEN to true,CREATE_NEW_PIN_CODE to false)
            )
    }

}