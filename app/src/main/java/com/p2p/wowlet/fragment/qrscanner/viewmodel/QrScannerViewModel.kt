package com.p2p.wowlet.fragment.qrscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.AddCoinItem

class QrScannerViewModel : BaseViewModel() {

    private val listAddCoinData = mutableListOf(
        AddCoinItem("P2P wallet", "Profile balance"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Tether", "Wallet balance")
    )

    private val _getAddCoinData by lazy { MutableLiveData<MutableList<AddCoinItem>>() }
    val getAddCoinData: LiveData<MutableList<AddCoinItem>> get() = _getAddCoinData

    fun getAddCoinList() {
        _getAddCoinData.value = listAddCoinData
    }

    fun goToFaceIdFragment() {
        _command.value =
            Command.NavigateFaceIdViewCommand(R.id.action_navigation_pin_code_to_navigation_face_id)
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_scanner_to_navigation_dashboard)
    }
}