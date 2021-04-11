package com.p2p.wowlet.notification.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel
import com.p2p.wowlet.dashboard.interactor.NotificationInteractor
import com.p2p.wowlet.dashboard.model.local.EnableNotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(val notificationInteractor: NotificationInteractor) : BaseViewModel() {
    private val _isSkipNotification by lazy { MutableLiveData<Unit>() }
    val isSkipNotification: LiveData<Unit> get() = _isSkipNotification
    private val _isAlreadyEnableNotification by lazy { MutableLiveData<Unit>() }
    val isAlreadyEnableNotification: LiveData<Unit> get() = _isAlreadyEnableNotification
    private val _showNotificationDialog: MutableLiveData<Unit> by lazy { MutableLiveData() }
    val showNotificationDialog: LiveData<Unit> get() = _showNotificationDialog

    init {
        notificationStatus()
    }

    fun openEnableNotificationDialog() {
        _showNotificationDialog.value = Unit
    }

    fun doThisLater() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationInteractor.enableNotification(
                EnableNotificationModel(
                    isEnable = false,
                    isNotWantEnable = true
                )
            )
        }
    }

    fun enableNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationInteractor.enableNotification(
                EnableNotificationModel(
                    isEnable = true,
                    isNotWantEnable = false
                )
            )
        }
    }

    private fun notificationStatus() {
        val data = notificationInteractor.isEnableNotification()
        if (data.isEnable) {
            _isAlreadyEnableNotification.value = Unit
        } else if (data.isNotWantEnable) {
            _isSkipNotification.value = Unit
        }
    }
}