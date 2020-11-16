package com.p2p.wowlet.fragment.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.local.AddCoinItem
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.EnterWallet

class DashboardViewModel(val dashboardInteractor: DashboardInteractor) : BaseViewModel() {
    private val listAddCoinData = mutableListOf(
        AddCoinItem("P2P wallet", "Profile balance"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Bitcoin", "12 000 US"),
        AddCoinItem("Tether", "Wallet balance")
    )
    private val listData = mutableListOf(WalletItem(""), WalletItem(""))

    private val _pages: MutableLiveData<List<EnterWallet>> by lazy { MutableLiveData() }
    val pages: LiveData<List<EnterWallet>> get() = _pages

    private val _getWalletData by lazy { MutableLiveData<MutableList<WalletItem>>() }
    val getWalletData: LiveData<MutableList<WalletItem>> get() = _getWalletData

    private val _getAddCoinData by lazy { MutableLiveData<MutableList<AddCoinItem>>() }
    val getAddCoinData: LiveData<MutableList<AddCoinItem>> get() = _getAddCoinData

    init {
        _getWalletData.value = listData
    }

    fun getAddCoinList() {
        _getAddCoinData.value = listAddCoinData
    }



    fun initReceiver() {
        val qrCode = dashboardInteractor.generateQRrCode()
        _pages.value = listOf(EnterWallet(qrCode, ""))
    }

    fun finishApp() {
        _command.value = FinishAppViewCommand()
    }

    fun goToScannerFragment() {
        _command.value =
            NavigateScannerViewCommand(R.id.action_navigation_dashboard_to_navigation_scanner)
    }

    fun goToReceiveFragment() {
        _command.value =
            NavigateReceiveViewCommand(R.id.action_navigation_dashboard_to_navigation_receive)
    }

    fun goToSendCoinFragment() {
        _command.value =
            NavigateSendCoinViewCommand(R.id.action_navigation_dashboard_to_navigation_send_coin)
    }

    fun goToSwapFragment() {
        _command.value =
            NavigateSwapViewCommand(R.id.action_navigation_dashboard_to_navigation_swap)
    }

    fun goToDetailSavingFragment() {
        _command.value =
            NavigateDetailSavingViewCommand(R.id.action_navigation_dashboard_to_navigation_detail_saving)
    }

    fun openAddCoinDialog() {
        _command.value = OpenAddCoinDialogViewCommand()
    }

    fun openProfileDialog() {
        _command.value = OpenProfileDialogViewCommand()
    }

    fun enterWalletDialog() {
        _command.value = EnterWalletDialogViewCommand()
    }

}