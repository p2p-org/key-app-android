package com.p2p.wowlet.fragment.splashscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.SplashData

class SplashScreenViewModel : BaseViewModel() {

    private val _pages: MutableLiveData<List<SplashData>> by lazy { MutableLiveData() }
    val pages: LiveData<List<SplashData>> get() = _pages

    fun initData(list: List<SplashData>) {
        _pages.value = list
    }
    fun finishApp() {
        _command.value =Command.FinishAppViewCommand()
    }
    fun goToRegLoginFragment() {
        _command.value = Command.NavigateRegLoginViewCommand(R.id.action_navigation_splash_screen_to_navigation_reg_login)
    }

}