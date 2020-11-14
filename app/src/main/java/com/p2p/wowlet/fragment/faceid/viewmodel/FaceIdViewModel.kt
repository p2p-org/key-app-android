package com.p2p.wowlet.fragment.faceid.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class FaceIdViewModel : BaseViewModel() {

    fun goToNotificationFragment() {
        _command.value =
            NavigateNotificationViewCommand(R.id.action_navigation_face_id_to_navigation_notification)
    }

    fun navigateUp() {
        _command.value =
            NavigateUpViewCommand(R.id.action_navigation_face_id_to_navigation_pin_code)
    }

}