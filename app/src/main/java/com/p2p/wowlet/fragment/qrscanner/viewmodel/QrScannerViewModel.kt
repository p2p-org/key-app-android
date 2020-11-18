package com.p2p.wowlet.fragment.qrscanner.viewmodel

import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel

class QrScannerViewModel : BaseViewModel() {



    fun goToFaceIdFragment() {
        _command.value =
            Command.NavigateFaceIdViewCommand(R.id.action_navigation_pin_code_to_navigation_face_id)
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_scanner_to_navigation_dashboard)
    }
}