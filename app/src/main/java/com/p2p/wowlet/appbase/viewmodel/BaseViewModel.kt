package com.p2p.wowlet.appbase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.p2p.wowlet.appbase.viewcommand.ViewCommand

abstract class BaseViewModel : ViewModel() {

    val _command = SingleLiveData<ViewCommand>()
    val command: LiveData<ViewCommand>
        get() = _command


}