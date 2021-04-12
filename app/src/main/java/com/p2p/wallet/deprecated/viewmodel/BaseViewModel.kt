package com.p2p.wallet.deprecated.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.p2p.wallet.deprecated.viewcommand.ViewCommand

@Deprecated("This will be deleted, migrating to MVP")
abstract class BaseViewModel : ViewModel() {

    val _command = SingleLiveEvent<ViewCommand>()
    val command: LiveData<ViewCommand>
        get() = _command
}