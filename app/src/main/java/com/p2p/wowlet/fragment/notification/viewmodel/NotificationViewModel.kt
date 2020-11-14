package com.p2p.wowlet.fragment.notification.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.backupingkey.BackingUpFromKeyDialog
import com.p2p.wowlet.fragment.notification.dialog.EnableNotificationDialog
import com.wowlet.domain.interactors.NotificationInteractor
import com.wowlet.entities.local.UserWalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(val notificationInteractor: NotificationInteractor) : BaseViewModel() {

    private val _showNotificationDialog: MutableLiveData<Unit> by lazy { MutableLiveData() }
    val showNotificationDialog: LiveData<Unit> get() = _showNotificationDialog

    fun goToRefFinishFragment() {
        _command.value =
            NavigateRegFinishViewCommand(R.id.action_navigation_notification_to_navigation_reg_finish)
    }

    fun navigateUp() {
        _command.value =
            NavigateUpViewCommand(R.id.action_navigation_notification_to_navigation_face_id)
    }

    fun openEnableNotificationDialog() {
        _showNotificationDialog.value = Unit
    }

    fun enableNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationInteractor.enableNotification(true)
        }
    }

}